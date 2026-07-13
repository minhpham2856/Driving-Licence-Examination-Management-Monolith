package examstaff.dao.impl;

import examstaff.dao.ExamEnrollmentSectionDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamEnrollmentSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamEnrollmentSectionDAOImpl extends DBContext implements ExamEnrollmentSectionDAO {

    private static final String COLUMNS = """
            ExamEnrollmentSectionId, ExamEnrollmentId, ExamSectionId, ExamAreaId,
            ExamDeviceId, Status, AllocatedAt, AllocatedBy, StartedAt, CompletedAt
            """;

    @Override
    public ExamEnrollmentSection getById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM ExamEnrollmentSection WHERE ExamEnrollmentSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
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

    @Override
    public ExamEnrollmentSection getByExamEnrollmentId(int examEnrollmentId) {
        String sql = "SELECT " + COLUMNS + " FROM ExamEnrollmentSection WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
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

    @Override
    public List<ExamEnrollmentSection> getByExamAndSection(int examId, int examSectionId) {
        String sql = "SELECT " + COLUMNS
                + " FROM ExamEnrollmentSection ees"
                + " JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId"
                + " WHERE ee.ExamId = ? AND ees.ExamSectionId = ?";
        List<ExamEnrollmentSection> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, examSectionId);
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

    @Override
    public boolean updateStatus(int examEnrollmentSectionId, String status) {
        String sql = "UPDATE ExamEnrollmentSection SET Status = ? WHERE ExamEnrollmentSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, examEnrollmentSectionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static ExamEnrollmentSection mapRow(ResultSet rs) throws SQLException {
        ExamEnrollmentSection row = new ExamEnrollmentSection();
        row.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
        row.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        int sectionId = rs.getInt("ExamSectionId");
        if (!rs.wasNull()) {
            row.setExamSectionId(sectionId);
        }
        int areaId = rs.getInt("ExamAreaId");
        if (!rs.wasNull()) {
            row.setExamAreaId(areaId);
        }
        int deviceId = rs.getInt("ExamDeviceId");
        if (!rs.wasNull()) {
            row.setExamDeviceId(deviceId);
        }
        row.setStatus(rs.getString("Status"));
        row.setAllocatedAt(rs.getTimestamp("AllocatedAt"));
        int allocatedBy = rs.getInt("AllocatedBy");
        if (!rs.wasNull()) {
            row.setAllocatedBy(allocatedBy);
        }
        row.setStartedAt(rs.getTimestamp("StartedAt"));
        row.setCompletedAt(rs.getTimestamp("CompletedAt"));
        return row;
    }
}

