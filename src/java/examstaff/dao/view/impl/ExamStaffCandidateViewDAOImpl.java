package examstaff.dao.view.impl;

import examstaff.dao.Db2CandidateSql;
import examstaff.dao.view.ExamStaffCandidateViewDAO;
import shared.dbconnection.DBContext;
import examstaff.dto.view.ExamStaffCandidate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JDBC implementation của {@link ExamStaffCandidateViewDAO}. */
public class ExamStaffCandidateViewDAOImpl extends DBContext implements ExamStaffCandidateViewDAO {

    /** {@inheritDoc} */
    @Override
    public List<ExamStaffCandidate> findByExamId(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamStaffCandidate> list = query(Db2CandidateSql.CANDIDATE_SELECT,
                " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId);
        if (list.isEmpty()) {
            list = query(Db2CandidateSql.CANDIDATE_SELECT_MINIMAL,
                    " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        for (ExamStaffCandidate row : findByExamId(examId)) {
            if (trimmed.equals(formatSbd(row.getCandidateNo()))) {
                return row;
            }
        }
        return null;
    }

    /** Chạy SELECT + WHERE và map danh sách thí sinh. */
    private List<ExamStaffCandidate> query(String selectSql, String whereSql, int bindInt) {
        List<ExamStaffCandidate> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return list;
        }
        String sql = selectSql + whereSql;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bindInt);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Ánh xạ ResultSet → {@link ExamStaffCandidate}. */
    private static ExamStaffCandidate mapRow(ResultSet rs) throws SQLException {
        ExamStaffCandidate row = new ExamStaffCandidate();
        row.setCandidateId(rs.getInt("id"));
        row.setExamId(rs.getInt("examId"));
        try {
            row.setExamEnrollmentId(rs.getInt("examEnrollmentId"));
        } catch (SQLException ignored) {
            row.setExamEnrollmentId(0);
        }
        row.setCandidateNo(rs.getInt("candidateNo"));
        row.setRegistrationType(rs.getString("registrationType"));
        row.setPaymentCompleted(readBit(rs, "isPaymentCompleted"));
        row.setPresent(readBit(rs, "isPresent"));
        row.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        row.setFullName(rs.getString("fullName"));
        row.setGovIdNo(rs.getString("govIdNo"));
        row.setDateOfBirth(rs.getDate("dateOfBirth"));
        row.setMale(readBit(rs, "gender"));
        row.setPhoneNo(rs.getString("phoneNo"));
        row.setEmail(rs.getString("email"));
        row.setPhotoUrl(rs.getString("photoUrl"));
        row.setLicenseCode(rs.getString("licenseCode"));
        row.setComputerCode(rs.getString("computerCode"));
        row.setAddress(rs.getString("address"));
        row.setReasonForTaking(rs.getString("reasonForTaking"));
        try {
            row.setTakeTheory(readNullableBoolean(rs, "takeTheory"));
            row.setTakePractical(readNullableBoolean(rs, "takePractical"));
        } catch (SQLException ignored) {
            row.setTakeTheory(null);
            row.setTakePractical(null);
        }
        row.setExamDate(rs.getDate("examDate"));
        try {
            row.setSectionStatus(rs.getString("sectionStatus"));
            row.setSignaturePrinted(readBit(rs, "signaturePrinted"));
        } catch (SQLException ignored) {
            row.setSectionStatus(null);
            row.setSignaturePrinted(false);
        }
        String notes = rs.getString("notes");
        row.setNotes(notes);
        boolean absent = readBit(rs, "isAbsent");
        if (!absent && notes != null && "Absent".equalsIgnoreCase(notes.trim())) {
            absent = true;
        }
        row.setAbsent(absent);
        row.setSuspended(readBit(rs, "isSuspended"));
        int areaIdVal = rs.getInt("allocatedAreaId");
        if (!rs.wasNull()) {
            row.setAllocatedAreaId(areaIdVal);
            row.setAllocatedAreaName(rs.getString("allocatedAreaName"));
        }
        try {
            int pracAreaId = rs.getInt("practicalAllocatedAreaId");
            if (!rs.wasNull()) {
                row.setPracticalAllocatedAreaId(pracAreaId);
                row.setPracticalAllocatedAreaName(rs.getString("practicalAllocatedAreaName"));
            }
        } catch (SQLException ignored) {
            // older selects may omit practical area
        }
        try {
            int theory = rs.getInt("theoryScore");
            if (!rs.wasNull()) {
                row.setTheoryScore(theory);
            }
            int practical = rs.getInt("practicalScore");
            if (!rs.wasNull()) {
                row.setPracticalScore(practical);
            }
        } catch (SQLException ignored) {
            // minimal select may omit scores
        }
        return row;
    }

    /** Đọc cột BIT; null → false. */
    private static boolean readBit(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return !rs.wasNull() && value;
    }

    /** Đọc cột BIT nullable. */
    private static Boolean readNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    /** Format SBD 3 chữ số. */
    private static String formatSbd(int candidateNo) {
        return String.format(Locale.ROOT, "%03d", candidateNo);
    }
}
