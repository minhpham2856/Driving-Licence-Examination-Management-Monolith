package examstaff.dao.impl;

import examstaff.dao.Db2CandidateSql;
import examstaff.dao.ExamStaffCandidateViewDAO;
import shared.dbconnection.DBContext;
import examstaff.dto.ExamStaffCandidate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Triển khai JDBC của ExamStaffCandidateViewDAO — read model thí sinh theo kỳ thi.
 *
 * SQL và fallback:
 * findByExamId chạy Db2CandidateSql.CANDIDATE_SELECT + WHERE ex.ExamId = ?;
 * nếu rỗng (schema thiếu cột điểm) fallback CANDIDATE_SELECT_MINIMAL — cùng pattern
 * ExamRegistrationDAOImpl.
 *
 * Tra SBD:
 * findByExamIdAndSbd format SBD 3 chữ số, duyệt list kỳ hoặc so khớp trực tiếp
 * tùy input — phục vụ gọi số / tra cứu nhanh trên màn staff.
 *
 * Map DTO:
 * Private query(...) map ResultSet → ExamStaffCandidate
 * (gọn hơn ExamRegistrationDTO, không method ghi).
 */
public class ExamStaffCandidateViewDAOImpl extends DBContext implements ExamStaffCandidateViewDAO {

    /**
     * Liệt kê thí sinh thuộc kỳ thi từ view SQL Db2CandidateSql.
     * Fallback sang CANDIDATE_SELECT_MINIMAL nếu SELECT đầy đủ không trả dữ liệu.
     * @param examId mã kỳ thi
     * @return danh sách ExamStaffCandidate; rỗng nếu examId không hợp lệ
     */
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

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh (SBD).
     * Duyệt danh sách kỳ thi và so khớp SBD đã format 3 chữ số.
     * @param examId mã kỳ thi
     * @param sbd    số báo danh (chuỗi, có thể có/không zero-pad)
     * @return thí sinh khớp hoặc null
     */
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

    /**
     * Chạy SELECT thí sinh (từ selectSql + whereSql) và ánh xạ danh sách.
     * @param selectSql phần SELECT (từ Db2CandidateSql)
     * @param whereSql  mệnh đề WHERE + ORDER BY
     * @param bindInt   giá trị bind cho placeholder đầu tiên (thường là examId)
     * @return danh sách thí sinh; rỗng nếu không có kết nối hoặc lỗi SQL
     */
    private List<ExamStaffCandidate> query(String selectSql, String whereSql, int bindInt) {
        List<ExamStaffCandidate> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return list;
        }
        String sql = selectSql + whereSql;
        // Chuẩn bị PreparedStatement với SQL SELECT thí sinh
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, bindInt);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ Db2CandidateSql) sang ExamStaffCandidate.
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO thí sinh view đầy đủ thông tin hiển thị
     * @throws SQLException nếu đọc cột bắt buộc thất bại
     */
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
            row.setWrongCriticalTheory(readBit(rs, "hasWrongCriticalTheory"));
            int practical = rs.getInt("practicalScore");
            if (!rs.wasNull()) {
                row.setPracticalScore(practical);
            }
        } catch (SQLException ignored) {
            // minimal select may omit scores
        }
        return row;
    }

    /**
     * Đọc cột BIT; giá trị SQL NULL được coi là false.
     * @param rs     ResultSet nguồn
     * @param column tên cột BIT
     * @return giá trị boolean (false nếu NULL)
     * @throws SQLException nếu đọc cột thất bại
     */
    private static boolean readBit(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return !rs.wasNull() && value;
    }

    /**
     * Đọc cột BIT nullable, trả null nếu SQL NULL.
     * @param rs     ResultSet nguồn
     * @param column tên cột BIT
     * @return Boolean hoặc null
     * @throws SQLException nếu đọc cột thất bại
     */
    private static Boolean readNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    /**
     * Format số báo danh thành chuỗi 3 chữ số (zero-pad).
     * @param candidateNo số thứ tự thí sinh
     * @return SBD dạng 001, 042, ...
     */
    private static String formatSbd(int candidateNo) {
        return String.format(Locale.ROOT, "%03d", candidateNo);
    }
}
