package candidate.dao.impl;

import candidate.dao.CandidateExamDAO;
import candidate.dto.CandidateExamContext;
import candidate.dto.ExamResultView;
import candidate.dto.QuestionView;
import shared.dbconnection.DBContext;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CandidateExamDAOImpl extends DBContext implements CandidateExamDAO {

    /**
     * Chuỗi: Candidate -> ExamEnrollment -> Exam -> Licence
     *        -> ExamSection (phần thi lý thuyết) -> ExamEnrollmentSection (dòng của thí sinh).
     * Lấy hạng từ Exam.LicenceId; thời lượng từ ExamSection.DurationMinutes; máy từ ExamEnrollmentSection.ExamDeviceId.
     */
    private static final String CTX_SQL = """
            SELECT TOP 1
                c.CandidateId, c.CandidateNumber, c.FullName, c.DateOfBirth,
                c.GovernmentIdNumber, c.Address, c.PhotoImageUrl,
                ee.ExamEnrollmentId,
                ex.ExamId, ex.CentreName, ex.LicenceId, lic.LicenceClass,
                sec.ExamSectionId, sec.DurationMinutes,
                ees.ExamEnrollmentSectionId, ees.ExamDeviceId
            FROM Candidate c
            JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
            JOIN Exam ex           ON ex.ExamId = ee.ExamId
            JOIN Licence lic       ON lic.LicenceId = ex.LicenceId
            JOIN ExamSection sec    ON sec.ExamId = ex.ExamId
                 AND (sec.SectionType LIKE N'%thuyết%' OR sec.SectionType LIKE '%theory%' OR sec.SectionType LIKE '%Theory%')
            JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                 AND ees.ExamSectionId = sec.ExamSectionId
            WHERE c.CandidateNumber = ?
            ORDER BY ex.StartTime DESC
            """;

    @Override
    public CandidateExamContext findContextByCandidateNumber(String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) return null;
        try (PreparedStatement ps = getConnection().prepareStatement(CTX_SQL)) {
            ps.setString(1, candidateNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                CandidateExamContext ctx = new CandidateExamContext();
                ctx.setCandidateId(rs.getInt("CandidateId"));
                ctx.setCandidateNumber(rs.getString("CandidateNumber"));
                ctx.setFullName(rs.getString("FullName"));
                Timestamp dob = rs.getTimestamp("DateOfBirth");
                ctx.setDobDisplay(dob == null ? "—" : new SimpleDateFormat("dd/MM/yyyy").format(dob));
                ctx.setCitizenId(rs.getString("GovernmentIdNumber"));
                ctx.setAddress(rs.getString("Address"));
                ctx.setPhotoUrl(rs.getString("PhotoImageUrl"));
                ctx.setExamLocation(rs.getString("CentreName"));
                ctx.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                ctx.setExamId(rs.getInt("ExamId"));
                ctx.setLicenceId(rs.getInt("LicenceId"));
                ctx.setLicenceClass(rs.getString("LicenceClass"));
                ctx.setExamSectionId(rs.getInt("ExamSectionId"));
                ctx.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
                int dev = rs.getInt("ExamDeviceId");
                ctx.setDeviceId(rs.wasNull() ? 0 : dev);
                Integer dur = (Integer) rs.getObject("DurationMinutes");
                if (dur != null) ctx.setDurationMinutes(dur);
                return ctx;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int startTheoryPaper(int examEnrollmentSectionId) {
        // đã có paper cho section này thì dùng lại + reset
        Integer existing = queryInt(
                "SELECT TOP 1 TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentSectionId = ? ORDER BY TheoryPaperId DESC",
                examEnrollmentSectionId);
        try {
            if (existing != null && existing > 0) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE TheoryPaper SET StartedAt = GETDATE(), SubmittedAt = NULL WHERE TheoryPaperId = ?")) {
                    ps.setInt(1, existing);
                    ps.executeUpdate();
                }
                touchSectionStarted(examEnrollmentSectionId);
                return existing;
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, examEnrollmentSectionId);
                if (ps.executeUpdate() > 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            touchSectionStarted(examEnrollmentSectionId);
                            return gk.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<QuestionView> loadRandomQuestions(int licenceId, int n) {
        String sql = """
                SELECT q.QuestionId, q.QuestionNumber, q.ImageUrl, q.CorrectAnswer, q.IsCritical
                FROM Question q
                JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId
                WHERE lq.LicenceId = ?
                """;
        List<QuestionView> pool = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuestionView q = new QuestionView();
                    q.setQuestionId(rs.getInt("QuestionId"));
                    q.setQuestionNumber(rs.getInt("QuestionNumber"));
                    q.setImageUrl(rs.getString("ImageUrl"));
                    q.setCorrectAnswer(rs.getString("CorrectAnswer"));
                    q.setCritical(rs.getBoolean("IsCritical"));
                    pool.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Collections.shuffle(pool);
        List<QuestionView> chosen = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();

        for (QuestionView q : pool) {                 // 1 câu điểm liệt trước
            if (q.isCritical()) { chosen.add(q); ids.add(q.getQuestionId()); break; }
        }
        for (QuestionView q : pool) {                 // fill phần còn lại
            if (chosen.size() >= n) break;
            if (ids.add(q.getQuestionId())) chosen.add(q);
        }
        for (int i = 0; i < chosen.size(); i++) chosen.get(i).setQuestionNumber(i + 1);
        return chosen;
    }

    @Override
    public ExamResultView submitAndGrade(int theoryPaperId, int examEnrollmentId, int examSectionId,
                                         int examEnrollmentSectionId,
                                         List<QuestionView> questions, Map<Integer, String> answers,
                                         int passThreshold) {
        int correct = 0, wrong = 0, unanswered = 0;
        boolean criticalFailed = false;

        try {
            if (theoryPaperId > 0) {
                try (PreparedStatement del = getConnection().prepareStatement(
                        "DELETE FROM CandidateAnswer WHERE TheoryPaperId = ?")) {
                    del.setInt(1, theoryPaperId);
                    del.executeUpdate();
                }
            }
            PreparedStatement ins = (theoryPaperId > 0) ? getConnection().prepareStatement(
                    "INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer) VALUES (?, ?, ?)") : null;

            for (QuestionView q : questions) {
                String ans = answers.get(q.getQuestionId());
                boolean blank = ans == null || ans.isBlank();
                boolean isCorrect = !blank && normalize(ans).equals(normalize(q.getCorrectAnswer()));

                if (blank) unanswered++;
                else if (isCorrect) correct++;
                else wrong++;
                if (q.isCritical() && !isCorrect) criticalFailed = true;

                if (ins != null) {
                    ins.setInt(1, theoryPaperId);
                    ins.setInt(2, q.getQuestionId());
                    if (blank) ins.setNull(3, Types.NVARCHAR);
                    else ins.setString(3, ans.trim());
                    ins.addBatch();
                }
            }
            if (ins != null) {
                ins.executeBatch();
                ins.close();
                try (PreparedStatement up = getConnection().prepareStatement(
                        "UPDATE TheoryPaper SET SubmittedAt = GETDATE() WHERE TheoryPaperId = ?")) {
                    up.setInt(1, theoryPaperId);
                    up.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean passed = correct >= passThreshold && !criticalFailed;

        int resultId = upsertExamResult(examEnrollmentId, passed);
        if (resultId > 0 && examSectionId > 0) saveExamScore(resultId, examSectionId, correct);
        touchSectionCompleted(examEnrollmentSectionId);

        ExamResultView v = new ExamResultView();
        v.setPassed(passed);
        v.setScore(correct);
        v.setCorrectCount(correct);
        v.setIncorrectCount(wrong);
        v.setUnansweredCount(unanswered);
        v.setTotalQuestions(questions.size());
        v.setCriticalFailed(criticalFailed);
        return v;
    }

    // ---------- helpers ----------

    private void touchSectionStarted(int examEnrollmentSectionId) {
        exec("UPDATE ExamEnrollmentSection SET StartedAt = GETDATE() WHERE ExamEnrollmentSectionId = ?",
                examEnrollmentSectionId);
    }

    private void touchSectionCompleted(int examEnrollmentSectionId) {
        exec("UPDATE ExamEnrollmentSection SET CompletedAt = GETDATE() WHERE ExamEnrollmentSectionId = ?",
                examEnrollmentSectionId);
    }

    private int upsertExamResult(int examEnrollmentId, boolean passed) {
        Integer id = queryInt("SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?", examEnrollmentId);
        try {
            if (id != null && id > 0) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE ExamResult SET IsPassed = ?, ResultDate = GETDATE() WHERE ExamResultId = ?")) {
                    ps.setBoolean(1, passed);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                return id;
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, examEnrollmentId);
                ps.setBoolean(2, passed);
                if (ps.executeUpdate() > 0) {
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) return gk.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** ExamScore.Score có CHECK 0..100. Số câu đúng (≤45) hợp lệ. */
    private void saveExamScore(int resultId, int examSectionId, int correctCount) {
        try {
            try (PreparedStatement del = getConnection().prepareStatement(
                    "DELETE FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?")) {
                del.setInt(1, resultId); del.setInt(2, examSectionId); del.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)")) {
                ps.setInt(1, resultId);
                ps.setInt(2, examSectionId);
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(Math.min(correctCount, 100)));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Chuẩn hóa A/B/C/D <-> 1/2/3/4 để chấm không lệ thuộc format seed. */
    private static String normalize(String a) {
        if (a == null) return "";
        String s = a.trim().toUpperCase();
        switch (s) {
            case "A": return "1";
            case "B": return "2";
            case "C": return "3";
            case "D": return "4";
            default:  return s;
        }
    }

    private Integer queryInt(String sql, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void exec(String sql, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
