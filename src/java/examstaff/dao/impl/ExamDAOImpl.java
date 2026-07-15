package examstaff.dao.impl;

import shared.dbconnection.DBContext;
import examstaff.dao.ExamDAO;
import examstaff.dto.ExamSummaryDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Triển khai JDBC của {@link ExamDAO} — đọc và cập nhật trạng thái kỳ thi trên bảng {@code Exam}.
 */
public class ExamDAOImpl extends DBContext implements ExamDAO {

    /** SELECT tóm tắt kỳ thi kèm hạng GPLX từ {@code Exam} JOIN {@code Licence}. */
    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS id, "
            + "e.ExamId AS examId, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Hạng ' + l.LicenceClass + N' - ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName, "
            + "1 AS examTypeId, "
            + "CAST(e.ExamDate AS DATE) AS examDate, "
            + "CAST(e.StartTime AS TIME) AS shiftStartTime, "
            + "CAST(e.EndTime AS TIME) AS shiftEndTime, "
            + "e.StartTime AS scheduledStartAt, "
            + "e.EndTime AS scheduledEndAt, "
            + "e.[Status] AS status, "
            + "e.StartTime AS createdAt, "
            + "l.LicenceClass AS licenseCode, "
            + "e.ExamCode AS examCode, "
            + "N'Lý thuyết + Thực hành' AS examTypeName "
            + "FROM Exam e "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId";

    /**
     * Lấy một kỳ thi theo mã từ bảng {@code Exam} (JOIN {@code Licence} để lấy hạng GPLX).
     *
     * @param id mã kỳ thi ({@code ExamId})
     * @return DTO tóm tắt kỳ thi, hoặc {@code null} nếu {@code id} không hợp lệ hoặc không tìm thấy
     */
    @Override
    public ExamSummaryDTO getById(int id) {
        if (id <= 0) {
            return null;
        }
        // Chuẩn bị PreparedStatement với SQL SELECT kỳ thi theo ExamId
        try (PreparedStatement ps = getConnection().prepareStatement(EXAM_SELECT + " WHERE e.ExamId = ?")) {
            // Gán tham số truy vấn
            ps.setInt(1, id);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    return mapResultSetToExam(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Cập nhật trường {@code Status} của kỳ thi trên bảng {@code Exam}.
     *
     * @param examId mã kỳ thi cần cập nhật
     * @param status trạng thái mới (ví dụ: {@code InProgress}, {@code Completed})
     * @return {@code true} nếu UPDATE ảnh hưởng ít nhất một dòng; {@code false} nếu thất bại
     */
    @Override
    public boolean updateStatus(int examId, String status) {
        String sql = "UPDATE Exam SET [Status] = ? WHERE ExamId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE trạng thái
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setString(1, status);
            ps.setInt(2, examId);
            // Thực thi UPDATE và trả kết quả
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kết thúc kỳ thi: ghi {@code Status} và {@code EndTime} vào bảng {@code Exam}.
     *
     * @param examId  mã kỳ thi
     * @param status  trạng thái kết thúc (ví dụ: {@code Completed})
     * @param endTime thời điểm kết thúc; nếu {@code null} dùng thời gian hiện tại
     * @return {@code true} nếu UPDATE thành công; {@code false} nếu thất bại
     */
    @Override
    public boolean finishExam(int examId, String status, Timestamp endTime) {
        String sql = "UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?";
        // Chuẩn bị PreparedStatement với SQL UPDATE trạng thái và EndTime
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setString(1, status);
            ps.setTimestamp(2, endTime != null ? endTime : new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, examId);
            // Thực thi UPDATE và trả kết quả
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ {@link #EXAM_SELECT}) sang {@link ExamSummaryDTO}.
     *
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO tóm tắt kỳ thi đã điền đủ trường
     * @throws SQLException nếu đọc cột thất bại
     */
    private ExamSummaryDTO mapResultSetToExam(ResultSet rs) throws SQLException {
        ExamSummaryDTO es = new ExamSummaryDTO();
        // Ánh xạ các cột định danh và tên kỳ thi
        es.setId(rs.getInt("id"));
        es.setExamId(rs.getInt("examId"));
        es.setExamName(rs.getString("examName"));
        es.setExamTypeId(rs.getInt("examTypeId"));
        es.setExamDate(rs.getDate("examDate"));
        es.setShiftStartTime(rs.getTime("shiftStartTime"));
        es.setShiftEndTime(rs.getTime("shiftEndTime"));
        Timestamp scheduledStart = rs.getTimestamp("scheduledStartAt");
        es.setScheduledStartAt(rs.wasNull() ? null : scheduledStart);
        Timestamp scheduledEnd = rs.getTimestamp("scheduledEndAt");
        es.setScheduledEndAt(rs.wasNull() ? null : scheduledEnd);
        es.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("createdAt");
        es.setCreatedAt(rs.wasNull() ? scheduledStart : created);
        es.setLicenseCode(rs.getString("licenseCode"));
        es.setExamCode(rs.getString("examCode"));
        es.setExamTypeName(rs.getString("examTypeName"));
        return es;
    }
}
