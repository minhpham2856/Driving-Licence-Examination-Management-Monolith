package examiner.dao.impl;

import examiner.dao.ExamEnrollmentSectionDAO;
import shared.dbconnection.DBContext;
import shared.enums.CandidateStatus;
import shared.enums.SectionType;
import shared.model.ExamEnrollmentSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JDBC implementation for ExamEnrollmentSection; examiner module DAO layer only.
public class ExamEnrollmentSectionDAOImpl extends DBContext implements ExamEnrollmentSectionDAO {

    private static final String BASE_SELECT =
            "SELECT ExamEnrollmentSectionId, ExamEnrollmentId, ExamSectionId, ExamAreaId, ExamDeviceId, "
            + "Status, AllocatedAt, AllocatedBy, StartedAt, CompletedAt, ResultPrintedAt FROM ExamEnrollmentSection";

    // Loads all section rows for one exam enrollment.
    @Override
    public List<ExamEnrollmentSection> getAllByEnrollmentId(int examEnrollmentId) {
        List<ExamEnrollmentSection> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Batch-loads section status strings keyed by enrollment id for one section type.
    @Override
    public Map<Integer, String> getStatusByEnrollmentIds(List<Integer> enrollmentIds, String sectionType) {
        Map<Integer, String> statuses = new HashMap<>();
        if (enrollmentIds == null || enrollmentIds.isEmpty()
                || sectionType == null || sectionType.isBlank()) {
            return statuses;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT ees.ExamEnrollmentId, ees.Status "
                + "FROM ExamEnrollmentSection ees "
                + "JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE es.SectionType = ? AND ees.ExamEnrollmentId IN (");
        for (int i = 0; i < enrollmentIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            ps.setString(1, sectionType.trim());
            for (int i = 0; i < enrollmentIds.size(); i++) {
                ps.setInt(i + 2, enrollmentIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statuses.put(rs.getInt("ExamEnrollmentId"), rs.getString("Status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    // Updates status by enrollment id and section type in the database.
    @Override
    public boolean updateStatusByEnrollmentIdAndSectionType(int examEnrollmentId, String sectionType,
            String status) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank()
                || status == null || status.isBlank()) {
            return false;
        }
        String sql = "UPDATE ees SET ees.Status = ? "
                + "FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.trim());
            ps.setInt(2, examEnrollmentId);
            ps.setString(3, sectionType.trim());
            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        // No matching section row yet: create one for this section type then update.
        return ensureSectionTypeAndSetStatus(examEnrollmentId, sectionType.trim(), status.trim());
    }

    // Finds existing section row id or inserts a NOT_STARTED row for the section.
    @Override
    public int getOrCreate(int examEnrollmentId, int examSectionId) {
        if (examEnrollmentId <= 0 || examSectionId <= 0) {
            return 0;
        }
        String findSql = "SELECT ExamEnrollmentSectionId FROM ExamEnrollmentSection "
                + "WHERE ExamEnrollmentId = ? AND ExamSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(findSql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, examSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentSectionId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        String insertSql = "INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, examSectionId);
            ps.setString(3, CandidateStatus.NOT_STARTED.getValue());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Finds theory section id by enrollment id for examiner workflow.
    @Override
    public int getIfTheorySectionIdByEnrollment(int examEnrollmentId) {
        String sql = "SELECT ees.ExamEnrollmentSectionId "
                + "FROM ExamEnrollmentSection ees "
                + "JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, SectionType.THEORY.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentSectionId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Updates exam area id by enrollment id and section type in the database.
    @Override
    public boolean updateExamAreaIdByEnrollmentIdAndSectionType(int examEnrollmentId, String sectionType,
            int examAreaId) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank() || examAreaId <= 0) {
            return false;
        }
        String sql = "UPDATE ees SET ees.ExamAreaId = ? "
                + "FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            ps.setInt(2, examEnrollmentId);
            ps.setString(3, sectionType.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Finds exam area id by enrollment id and section type for examiner workflow.
    @Override
    public int getIfAreaIdByEnrollmentAndSection(int examEnrollmentId, String sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank()) {
            return 0;
        }
        String sql = "SELECT ees.ExamAreaId "
                + "FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, sectionType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int areaId = rs.getInt("ExamAreaId");
                    return rs.wasNull() ? 0 : areaId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Creates the section-type row when missing, then sets status.
    private boolean ensureSectionTypeAndSetStatus(int examEnrollmentId, String sectionType, String status) {
        String findExamSql = "SELECT ExamId FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        int examId = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(findExamSql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    examId = rs.getInt("ExamId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        if (examId <= 0) {
            return false;
        }
        String sectionSql = "SELECT TOP 1 ExamSectionId FROM ExamSection WHERE ExamId = ? AND SectionType = ?";
        int examSectionId = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(sectionSql)) {
            ps.setInt(1, examId);
            ps.setString(2, sectionType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    examSectionId = rs.getInt("ExamSectionId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        int sectionRowId = getOrCreate(examEnrollmentId, examSectionId);
        if (sectionRowId <= 0) {
            return false;
        }
        String updateSql = "UPDATE ExamEnrollmentSection SET Status = ? WHERE ExamEnrollmentSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(updateSql)) {
            ps.setString(1, status);
            ps.setInt(2, sectionRowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Creates the section-type row when missing, then sets NOT_STARTED status.
    @Override
    public boolean ensureSectionRow(int examEnrollmentId, String sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank()) {
            return false;
        }
        String findSql = "SELECT COUNT(*) FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(findSql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, sectionType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return ensureSectionTypeAndSetStatus(
                examEnrollmentId, sectionType.trim(), CandidateStatus.NOT_STARTED.getValue());
    }

    // Batch-loads whether result forms were printed for each enrollment in a section.
    @Override
    public Map<Integer, Boolean> getResultPrintedByEnrollmentIds(List<Integer> enrollmentIds, String sectionType) {
        Map<Integer, Boolean> printed = new HashMap<>();
        if (enrollmentIds == null || enrollmentIds.isEmpty()
                || sectionType == null || sectionType.isBlank()) {
            return printed;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT ees.ExamEnrollmentId, ees.ResultPrintedAt "
                + "FROM ExamEnrollmentSection ees "
                + "JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE es.SectionType = ? AND ees.ExamEnrollmentId IN (");
        for (int i = 0; i < enrollmentIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            ps.setString(1, sectionType.trim());
            for (int i = 0; i < enrollmentIds.size(); i++) {
                ps.setInt(i + 2, enrollmentIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    printed.put(rs.getInt("ExamEnrollmentId"), rs.getTimestamp("ResultPrintedAt") != null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return printed;
    }

    // Returns true when ResultPrintedAt is set for the enrollment section row.
    @Override
    public boolean isResultPrinted(int examEnrollmentId, String sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank()) {
            return false;
        }
        String sql = "SELECT ees.ResultPrintedAt "
                + "FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, sectionType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("ResultPrintedAt") != null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Stamps ResultPrintedAt on the section row when the result form is printed.
    @Override
    public boolean markResultPrinted(int examEnrollmentId, String sectionType) {
        if (examEnrollmentId <= 0 || sectionType == null || sectionType.isBlank()) {
            return false;
        }
        if (!ensureSectionRow(examEnrollmentId, sectionType)) {
            return false;
        }
        String sql = "UPDATE ees SET ees.ResultPrintedAt = GETDATE() "
                + "FROM ExamEnrollmentSection ees "
                + "INNER JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "WHERE ees.ExamEnrollmentId = ? AND es.SectionType = ? "
                + "AND ees.ResultPrintedAt IS NULL";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, sectionType.trim());
            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isResultPrinted(examEnrollmentId, sectionType);
    }

    // Private helper: map.
    private static ExamEnrollmentSection map(ResultSet rs) throws SQLException {
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
        row.setResultPrintedAt(rs.getTimestamp("ResultPrintedAt"));
        return row;
    }
}
