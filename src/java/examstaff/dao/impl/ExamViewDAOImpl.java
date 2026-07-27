package examstaff.dao.impl;

import examstaff.dao.Db2ExamSummarySql;
import examstaff.dao.ExamViewDAO;
import examstaff.dto.ExamSummaryDTO;
import shared.dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model kỳ thi (một hàng / Exam) cho sidebar picker và list kỳ.
 * examId trên view = ExamId để tương thích UI cũ.
 *
 * Cách hoạt động:
 * Mọi query bắt đầu từ examstaff.dao.Db2ExamSummarySql.EXAM_SUMMARY_SELECT,
 * rồi gắn ORDER BY / WHERE tùy method (findAllOrdered, findByExamId, …).
 * Khác ExamDAOImpl: class này thiên về <b>đọc danh sách / view</b>; ghi Status vẫn qua ExamDAO.
 */
public class ExamViewDAOImpl extends DBContext implements ExamViewDAO {

    /**
     * Liệt kê mọi kỳ thi theo thứ tự ngày/ca giảm dần từ bảng Exam.
     */
    @Override
    public List<ExamSummaryDTO> findAllOrdered() {
        return fetchList(Db2ExamSummarySql.EXAM_SUMMARY_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, e.StartTime DESC");
    }

    /**
     * Tìm một kỳ thi theo mã từ Exam JOIN Licence.
     */
    @Override
    public ExamSummaryDTO findByExamId(int examId) {
        if (examId <= 0) {
            return null;
        }
        return fetchOne(Db2ExamSummarySql.EXAM_SUMMARY_SELECT + " WHERE e.ExamId = ?", examId);
    }

    private List<ExamSummaryDTO> fetchList(String sql) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExamSummaryDTO fetchOne(String sql, int examId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
        dto.setExamPassword(rs.getString("examPassword"));
        return dto;
    }
}
