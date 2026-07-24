package examstaff.dao.impl;

import examstaff.dao.Db2ExamSummarySql;
import examstaff.dao.ExamDAO;
import examstaff.dto.ExamSummaryDTO;
import shared.dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Triển khai JDBC của ExamDAO — đọc và cập nhật trạng thái kỳ thi trên bảng Exam.
 *
 * SQL tóm tắt kỳ:
 * Dùng chung examstaff.dao.Db2ExamSummarySql.EXAM_SUMMARY_SELECT với ExamViewDAOImpl
 * để cột map ExamSummaryDTO không lệch. getById chỉ thêm WHERE e.ExamId = ?.
 *
 * Status trên DB vs Call Board:
 * updateStatus ghi Exam.Status (Chưa diễn ra / Đang diễn ra / …).
 * Pause gọi số runtime nằm trên CallBoardState.examPaused — không qua class này.
 */
public class ExamDAOImpl extends DBContext implements ExamDAO {

    /**
     * Lấy một kỳ thi theo mã từ bảng Exam (JOIN Licence để lấy hạng GPLX).
     */
    @Override
    public ExamSummaryDTO getById(int id) {
        if (id <= 0) {
            return null;
        }
        String sql = Db2ExamSummarySql.EXAM_SUMMARY_SELECT + " WHERE e.ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật trường Status của kỳ thi trên bảng Exam.
     */
    @Override
    public boolean updateStatus(int examId, String status) {
        String sql = "UPDATE Exam SET [Status] = ? WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kết thúc kỳ thi: ghi Status và EndTime vào bảng Exam.
     */
    @Override
    public boolean finishExam(int examId, String status, Timestamp endTime) {
        String sql = "UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, endTime != null ? endTime : new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, examId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamSummaryDTO mapResultSetToExam(ResultSet rs) throws SQLException {
        ExamSummaryDTO es = new ExamSummaryDTO();
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
