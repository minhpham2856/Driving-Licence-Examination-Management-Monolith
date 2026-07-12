package examstaff.dao.impl;

import examstaff.dbconnection.DBContext;
import examstaff.dao.ExamDAO;
import examstaff.dto.ExamSummaryDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * JDBC implementation of ExamDAO — get/update status cho điều khiển kỳ thi.
 */
public class ExamDAOImpl extends DBContext implements ExamDAO {

    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS id, "
            + "e.ExamId AS examId, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Hạng ' + l.LicenceClass + N' — ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName, "
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

    @Override
    public ExamSummaryDTO getById(int id) {
        if (id <= 0) {
            return null;
        }
        try (PreparedStatement ps = getConnection().prepareStatement(EXAM_SELECT + " WHERE e.ExamId = ?")) {
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
