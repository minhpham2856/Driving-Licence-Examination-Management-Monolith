package examstaff.dao.impl;

import examstaff.dao.ExamViewDAO;
import shared.dbconnection.DBContext;
import examstaff.dto.ExamSummaryDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model kỳ thi (một hàng / Exam).
 * {@code examId} trên view = {@code ExamId} để tương thích UI cũ.
 */
public class ExamViewDAOImpl extends DBContext implements ExamViewDAO {

    /** SELECT tóm tắt kỳ thi + hạng GPLX từ {@code Exam} JOIN {@code Licence}. */
    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS examId, "
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
     * Liệt kê mọi kỳ thi theo thứ tự ngày/ca giảm dần từ bảng {@code Exam}.
     *
     * @return danh sách {@link ExamSummaryDTO} tóm tắt kỳ thi
     */
    @Override
    public List<ExamSummaryDTO> findAllOrdered() {
        return fetchList(EXAM_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, e.StartTime DESC");
    }

    /**
     * Tìm một kỳ thi theo mã từ {@code Exam} JOIN {@code Licence}.
     *
     * @param examId mã kỳ thi ({@code ExamId})
     * @return DTO tóm tắt hoặc {@code null} nếu không hợp lệ hoặc không tìm thấy
     */
    @Override
    public ExamSummaryDTO findByExamId(int examId) {
        if (examId <= 0) {
            return null;
        }
        return fetchOne(EXAM_SELECT + " WHERE e.ExamId = ?", examId);
    }

    /**
     * Chạy SELECT không tham số, ánh xạ toàn bộ hàng sang {@link ExamSummaryDTO}.
     *
     * @param sql câu SELECT đầy đủ (kèm ORDER BY nếu cần)
     * @return danh sách DTO; rỗng nếu lỗi SQL
     */
    private List<ExamSummaryDTO> fetchList(String sql) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        // Chuẩn bị PreparedStatement với SQL SELECT danh sách kỳ thi
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Duyệt ResultSet và ánh xạ từng dòng
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Chạy SELECT một hàng theo {@code examId}.
     *
     * @param sql    câu SELECT có placeholder {@code ?} cho ExamId
     * @param examId mã kỳ thi cần lọc
     * @return DTO tóm tắt hoặc {@code null} nếu không tìm thấy
     */
    private ExamSummaryDTO fetchOne(String sql, int examId) {
        // Chuẩn bị PreparedStatement với SQL SELECT kỳ thi theo ExamId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ {@link #EXAM_SELECT}) sang {@link ExamSummaryDTO}.
     *
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO tóm tắt kỳ thi
     * @throws SQLException nếu đọc cột thất bại
     */
    private static ExamSummaryDTO mapRow(ResultSet rs) throws SQLException {
        ExamSummaryDTO dto = new ExamSummaryDTO();
        int examId = rs.getInt("examId");
        dto.setId(examId);
        dto.setExamId(examId);
        dto.setExamName(rs.getString("examName"));
        dto.setExamTypeId(rs.getInt("examTypeId"));
        dto.setExamDate(rs.getDate("examDate"));
        dto.setShiftStartTime(rs.getTime("shiftStartTime"));
        dto.setShiftEndTime(rs.getTime("shiftEndTime"));
        Timestamp scheduledStart = rs.getTimestamp("scheduledStartAt");
        dto.setScheduledStartAt(scheduledStart);
        dto.setScheduledEndAt(rs.getTimestamp("scheduledEndAt"));
        dto.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        dto.setCreatedAt(createdAt != null ? createdAt : scheduledStart);
        dto.setLicenseCode(rs.getString("licenseCode"));
        dto.setExamCode(rs.getString("examCode"));
        dto.setExamTypeName(rs.getString("examTypeName"));
        return dto;
    }
}
