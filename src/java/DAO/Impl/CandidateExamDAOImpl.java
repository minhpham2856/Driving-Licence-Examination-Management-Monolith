package DAO.Impl;

import DAO.CandidateExamDAO;
import DBConnection.DBContext;
import Models.CandidateExamContext;
import Models.ExamResultView;
import Models.Question;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CandidateExamDAOImpl extends DBContext implements CandidateExamDAO {

    // Candidate -> Exam_Candidate -> Session/Exam ; ExamRegistration -> Licence (háº¡ng Ä‘á»ƒ bá»‘c Ä‘á»�).
    private static final String CTX_SQL = """
            SELECT TOP 1
                c.CandidateId, c.CandidateNumber, c.FullName, c.DateOfBirth,
                c.GovernmentIdNumber, c.Address, c.PhotoImageUrl,
                ec.ExamCandidateId, ec.SessionId,
                reg.LicenceId, lic.LicenceClass,
                ex.CentreName
            FROM Candidate c
            JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
            JOIN [Session] s       ON s.SessionId = ec.SessionId
            JOIN Exam ex           ON ex.ExamId = ec.ExamId
            JOIN ExamRegistration reg ON reg.ExamRegistrationId = c.ExamRegistrationId
            JOIN Licence lic       ON lic.LicenceId = reg.LicenceId
            WHERE c.CandidateNumber = ?
            ORDER BY CASE WHEN s.[Status] IN ('InProgress','Open') THEN 0 ELSE 1 END,
                     s.StartTime DESC
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
                ctx.setDobDisplay(dob == null ? "â€”" : new SimpleDateFormat("dd/MM/yyyy").format(dob));
                ctx.setCitizenId(rs.getString("GovernmentIdNumber"));
                ctx.setAddress(rs.getString("Address"));
                ctx.setPhotoUrl(rs.getString("PhotoImageUrl"));
                ctx.setExamLocation(rs.getString("CentreName"));
                ctx.setExamCandidateId(rs.getInt("ExamCandidateId"));
                ctx.setSessionId(rs.getInt("SessionId"));
                ctx.setLicenceId(rs.getInt("LicenceId"));
                ctx.setLicenceClass(rs.getString("LicenceClass"));
                return ctx;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MÃ¡y thi phÃ¢n sáºµn:
     *  1) TheoryPaper Ä‘Ã£ táº¡o trÆ°á»›c (giÃ¡m thá»‹ phÃ¢n mÃ¡y) -> dÃ¹ng Ä‘Ãºng mÃ¡y Ä‘Ã³.
     *  2) MÃ¡y Available/Operational trong khu vá»±c cá»§a ca thi (Session_ExamArea).
     *  3) Báº¥t ká»³ mÃ¡y kháº£ dá»¥ng nÃ o.
     *  4) Fallback cuá»‘i: mÃ¡y Ä‘áº§u tiÃªn (trÃ¡nh cháº·n luá»“ng khi demo).
     */
    @Override
    public int findAssignedDevice(int examCandidateId, int sessionId) {
        Integer d = queryInt(
                "SELECT TOP 1 ExamDeviceId FROM TheoryPaper WHERE ExamCandidateId = ? ORDER BY TheoryPaperId DESC",
                examCandidateId);
        if (d != null && d > 0) return d;

        d = queryInt("""
                SELECT TOP 1 dev.ExamDeviceId
                FROM ExamDevice dev
                JOIN Session_ExamArea sea ON sea.ExamAreaId = dev.ExamAreaId
                WHERE sea.SessionId = ? AND dev.[Status] IN ('Available','Operational')
                ORDER BY dev.ExamDeviceId
                """, sessionId);
        if (d != null && d > 0) return d;

        d = queryInt("SELECT TOP 1 ExamDeviceId FROM ExamDevice WHERE [Status] IN ('Available','Operational') ORDER BY ExamDeviceId");
        if (d != null && d > 0) return d;

        d = queryInt("SELECT TOP 1 ExamDeviceId FROM ExamDevice ORDER BY ExamDeviceId");
        return d == null ? 0 : d;
    }

    @Override
    public int startTheoryPaper(int examCandidateId, int deviceId) {
        Integer existing = queryInt(
                "SELECT TOP 1 TheoryPaperId FROM TheoryPaper WHERE ExamCandidateId = ? ORDER BY TheoryPaperId DESC",
                examCandidateId);
        try {
            if (existing != null && existing > 0) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE TheoryPaper SET StartedAt = GETDATE(), SubmittedAt = NULL WHERE TheoryPaperId = ?")) {
                    ps.setInt(1, existing);
                    ps.executeUpdate();
                }
                return existing;
            }
            String sql = deviceId > 0
                    ? "INSERT INTO TheoryPaper (ExamCandidateId, ExamDeviceId, StartedAt) VALUES (?, ?, GETDATE())"
                    : null;
            if (sql == null) return 0; // khÃ´ng cÃ³ mÃ¡y nÃ o -> khÃ´ng táº¡o Ä‘Æ°á»£c (ExamDeviceId NOT NULL)
            try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, examCandidateId);
                ps.setInt(2, deviceId);
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

    @Override
    public List<Question> loadRandomQuestions(int licenceId, int n) {
        String sql = """
                SELECT q.QuestionId, q.QuestionNumber, q.ImageUrl, q.CorrectAnswer, q.IsCritical
                FROM Question q
                JOIN Licence_Question lq ON lq.QuestionId = q.QuestionId
                WHERE lq.LicenceId = ?
                """;
        List<Question> pool = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
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
        List<Question> chosen = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();

        // 1 cÃ¢u Ä‘iá»ƒm liá»‡t trÆ°á»›c (náº¿u bá»™ Ä‘á»� cÃ³)
        for (Question q : pool) {
            if (q.isCritical()) { chosen.add(q); ids.add(q.getQuestionId()); break; }
        }
        // fill pháº§n cÃ²n láº¡i, trÃ¡nh trÃ¹ng
        for (Question q : pool) {
            if (chosen.size() >= n) break;
            if (ids.add(q.getQuestionId())) chosen.add(q);
        }
        // Ä‘Ã¡nh sá»‘ hiá»ƒn thá»‹ 1..N
        for (int i = 0; i < chosen.size(); i++) chosen.get(i).setQuestionNumber(i + 1);
        return chosen;
    }

    @Override
    public ExamResultView submitAndGrade(int theoryPaperId, int examCandidateId,
                                         List<Question> questions, Map<Integer, String> answers,
                                         int passThreshold) {
        int correct = 0, wrong = 0, unanswered = 0;
        boolean criticalFailed = false;

        try {
            // ghi cÃ¢u tráº£ lá»�i (ghi Ä‘Ã¨ náº¿u ná»™p láº¡i)
            if (theoryPaperId > 0) {
                try (PreparedStatement del = getConnection().prepareStatement(
                        "DELETE FROM CandidateAnswer WHERE TheoryPaperId = ?")) {
                    del.setInt(1, theoryPaperId);
                    del.executeUpdate();
                }
            }
            PreparedStatement ins = null;
            if (theoryPaperId > 0) {
                ins = getConnection().prepareStatement(
                        "INSERT INTO CandidateAnswer (TheoryPaperId, QuestionId, Answer) VALUES (?, ?, ?)");
            }

            for (Question q : questions) {
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

        int resultId = upsertExamResult(examCandidateId, passed);
        if (resultId > 0) saveExamScore(resultId, correct);

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

    private int upsertExamResult(int examCandidateId, boolean passed) {
        Integer id = queryInt("SELECT ExamResultId FROM ExamResult WHERE ExamCandidateId = ?", examCandidateId);
        try {
            if (id != null && id > 0) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE ExamResult SET IsPassed = ?, ResultDate = GETDATE(), UpdatedAt = GETDATE() WHERE ExamResultId = ?")) {
                    ps.setBoolean(1, passed);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                return id;
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO ExamResult (ExamCandidateId, IsPassed) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, examCandidateId);
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

    private void saveExamScore(int resultId, int correctCount) {
        Integer sectionId = queryInt(
                "SELECT TOP 1 ExamSectionId FROM ExamSection " +
                "WHERE SectionName LIKE N'%lÃ½ thuyáº¿t%' OR SectionName LIKE '%theory%' ORDER BY ExamSectionId");
        if (sectionId == null) sectionId = queryInt("SELECT TOP 1 ExamSectionId FROM ExamSection ORDER BY ExamSectionId");
        if (sectionId == null) return; // khÃ´ng cÃ³ section -> bá»� qua, ExamResult lÃ  Ä‘á»§ cho trang káº¿t quáº£
        try {
            try (PreparedStatement del = getConnection().prepareStatement(
                    "DELETE FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?")) {
                del.setInt(1, resultId); del.setInt(2, sectionId); del.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)")) {
                ps.setInt(1, resultId);
                ps.setInt(2, sectionId);
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(correctCount));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Chuáº©n hÃ³a A/B/C/D <-> 1/2/3/4 Ä‘á»ƒ cháº¥m khÃ´ng lá»‡ thuá»™c format seed. */
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
}
