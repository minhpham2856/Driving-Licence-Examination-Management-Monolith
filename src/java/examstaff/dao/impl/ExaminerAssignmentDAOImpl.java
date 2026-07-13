package examstaff.dao.impl;



import examstaff.dbconnection.DBContext;

import examstaff.dao.ExaminerAssignmentDAO;
import examstaff.dto.ExaminerSlotDTO;

import examstaff.enums.UserRole;
import examstaff.model.Profile;
import examstaff.dto.UserDTO;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation of {@link ExaminerAssignmentDAO}.
public class ExaminerAssignmentDAOImpl extends DBContext implements ExaminerAssignmentDAO {

    // Entity name used in the Audit table for examiner-area mappings.
    private static final String ROOM_MAPPING_ENTITY = "ExaminerSchedule";

    // SQL to list all active examiners with their profile and role info.
    private static final String EXAMINER_SELECT = """
            SELECT u.UserId,
                   u.Username,
                   u.Email,
                   u.PasswordHash,
                   u.RoleId,
                   u.IsActive,
                   r.RoleName,
                   p.ProfileId,
                   p.FullName,
                   p.DateOfBirth,
                   p.PhoneNumber,
                   p.Sex,
                   p.GovernmentIdNumber,
                   p.Address
            FROM [User] u
            INNER JOIN [Role] r ON r.RoleId = u.RoleId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            WHERE r.RoleName IN (?, N'Examiner') AND u.IsActive = 1
            ORDER BY p.FullName, u.Username
            """;

    private static final String SLOT_SELECT =
            "SELECT esch.ExaminerScheduleId AS ExamExaminerId, "
            + "esch.ExamId AS examId, "
            + "esch.ExaminerId AS examinerUserId, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Hạng ' + l.LicenceClass + N' — ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS examName, "
            + "eu.Username AS examinerUsername, "
            + "ep.FullName AS examinerName, "
            + "CAST(esch.ExamAreaId AS VARCHAR(20)) AS mappingEntityId, "
            + "ea.ExamAreaId AS areaId, "
            + "ea.AreaName AS areaName, "
            + "ea.AreaType AS areaType, "
            + "CASE "
            + "    WHEN COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Thực hành%' "
            + "      OR COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Sa hình%' "
            + "      OR COALESCE(esect.SectionType, ea.AreaType) LIKE '%Practical%' THEN 2 "
            + "    WHEN COALESCE(esect.SectionType, ea.AreaType) LIKE N'%Đường%' "
            + "      OR COALESCE(esect.SectionType, ea.AreaType) LIKE '%Road%' THEN 4 "
            + "    ELSE 1 "
            + "END AS examTypeId, "
            + "COALESCE(esect.SectionType, ea.AreaType) AS examTypeName "
            + "FROM ExaminerSchedule esch "
            + "JOIN Exam e ON e.ExamId = esch.ExamId "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId "
            + "JOIN [User] eu ON eu.UserId = esch.ExaminerId "
            + "LEFT JOIN Profile ep ON ep.UserId = eu.UserId "
            + "LEFT JOIN ExamArea ea ON ea.ExamAreaId = esch.ExamAreaId "
            + "LEFT JOIN ExamSection esect ON esect.ExamSectionId = esch.ExamSectionId";

    // Retrieves all active examiner users with their profiles from the database
    @Override
    public List<UserDTO> getActiveExaminers() {
        // List to hold the examiner DTOs
        List<UserDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(EXAMINER_SELECT)) {
            ps.setString(1, UserRole.SAT_HACH_VIEN.getDisplayName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapExaminer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Inserts an ExaminerSchedule row and an audit mapping in a single transaction.
    @Override
    public boolean assign(ExaminerSlotDTO slot) {
        if (slot == null || slot.getExamId() <= 0 || slot.getExaminerUserId() <= 0) {
            return false;
        }
        // Build the composite entity ID for the audit mapping (examId:areaId:examinerId)
        String entityId = buildMappingEntityId(slot.getExamId(), slot.getAreaId(), slot.getExaminerUserId());
        String existingAssignmentSql = """
                SELECT TOP 1 esch.ExaminerScheduleId
                FROM ExaminerSchedule esch
                WHERE esch.ExaminerId = ?
                  AND esch.ExamId = ?
                  AND esch.ExamAreaId <> ?
                """;
        String insertAssignment = """
                INSERT INTO ExaminerSchedule (ExamId, ExaminerId, ExamAreaId, ExamSectionId, AssignedBy, AssignedAt)
                VALUES (?, ?, ?, COALESCE((
                    SELECT TOP 1 es.ExamSectionId
                    FROM ExamSection es
                    WHERE es.ExamId = ?
                      AND (
                        (? = N'Lý thuyết' AND es.SectionType = N'Lý thuyết')
                        OR (? = N'Thực hành' AND (
                            es.SectionType LIKE N'%Thực hành%'
                            OR es.SectionType LIKE N'%Sa hình%'
                            OR es.SectionType LIKE N'%Đường%'
                        ))
                      )
                    ORDER BY es.ExamSectionId
                ), (
                    SELECT TOP 1 es.ExamSectionId
                    FROM ExamSection es
                    WHERE es.ExamId = ?
                    ORDER BY es.ExamSectionId
                )), ?, GETDATE())
                """;
        // SQL: delete any existing audit mapping for this entity (idempotent cleanup)
        String deleteMapping = """
                DELETE FROM Audit
                WHERE EntityName = ? AND EntityId = ?
                """;
        // SQL: insert the new audit mapping to track the area assignment
        String insertMapping = """
                INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'ASSIGN', ?, ?, ?, GETDATE())
                """;
        try {
            // Begin transaction — both assignment and audit mapping must succeed together
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(existingAssignmentSql)) {
                ps.setInt(1, slot.getExaminerUserId());
                ps.setInt(2, slot.getExamId());
                ps.setInt(3, slot.getAreaId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        getConnection().rollback();
                        return false;
                    }
                }
            }
            // Step 1: insert the ExaminerSchedule row
            try (PreparedStatement ps = getConnection().prepareStatement(insertAssignment)) {
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                String areaType = slot.getAreaType() != null ? slot.getAreaType().trim() : "";
                ps.setInt(1, slot.getExamId());
                ps.setInt(2, slot.getExaminerUserId());
                ps.setInt(3, slot.getAreaId());
                ps.setInt(4, slot.getExamId());
                ps.setString(5, areaType);
                ps.setString(6, areaType);
                ps.setInt(7, slot.getExamId());
                ps.setInt(8, assignedBy);
                ps.executeUpdate();
            }
            // Step 2: delete any stale audit mapping for this entity ID
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }
            // Step 3: insert the new audit mapping with the assigner's user ID
            try (PreparedStatement ps = getConnection().prepareStatement(insertMapping)) {
                // Use the assigner's user ID, defaulting to system user 3 if not provided
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                ps.setInt(1, assignedBy);
                ps.setString(2, ROOM_MAPPING_ENTITY);
                ps.setString(3, entityId);
                // Store the area name as the new value (fallback to area ID)
                ps.setString(4, slot.getAreaName() != null ? slot.getAreaName() : String.valueOf(slot.getAreaId()));
                ps.executeUpdate();
            }
            // Commit the transaction — all three operations succeeded
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            // Rollback the transaction on any error
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            // Error codes 2627 and 2601 indicate unique constraint violations (duplicate assignment)
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }
            // Log unexpected SQL errors
            e.printStackTrace();
        } finally {
            // Restore auto-commit mode regardless of outcome
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    // Removes a ExaminerSchedule row and its audit mapping in a single transaction.
    @Override
    public boolean remove(String slotKey) {
        // Parse the composite slot key into its three integer components
        int[] parts = parseSlotKey(slotKey);
        // Return false if the key is malformed (null or wrong number of parts)
        if (parts == null) {
            return false;
        }
        // Extract the individual components from the parsed array
        int examId = parts[0];
        int areaId = parts[1];
        int examinerId = parts[2];
        // Rebuild the audit entity ID from the parsed components
        String entityId = buildMappingEntityId(examId, areaId, examinerId);
        // SQL: delete the ExaminerSchedule row
        String deleteAssignment = "DELETE FROM ExaminerSchedule WHERE ExamId = ? AND ExaminerId = ?";
        // SQL: delete the audit mapping row
        String deleteMapping = "DELETE FROM Audit WHERE EntityName = ? AND EntityId = ?";
        try {
            // Begin transaction — both deletions must succeed together
            getConnection().setAutoCommit(false);
            // Step 1: delete the ExaminerSchedule row
            try (PreparedStatement ps = getConnection().prepareStatement(deleteAssignment)) {
                ps.setInt(1, examId);
                ps.setInt(2, examinerId);
                ps.executeUpdate();
            }
            // Step 2: delete the audit mapping row
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }
            // Commit the transaction — both deletions succeeded
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            // Rollback the transaction on any error
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            // Log unexpected SQL errors
            e.printStackTrace();
        } finally {
            // Restore auto-commit mode regardless of outcome
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    // Retrieves all assignment slots for a specific exam, ordered by area name
    @Override
    public List<ExaminerSlotDTO> getByExamId(int examId) {
        String sql = SLOT_SELECT + " WHERE esch.ExamId = ? ORDER BY ea.AreaName, esch.ExaminerScheduleId";
        return querySlots(sql, ps -> ps.setInt(1, examId));
    }

    // Executes a parameterised slot query and maps result rows to ExaminerSlotDTO objects.
    private List<ExaminerSlotDTO> querySlots(String sql, SqlBinder binder) {
        // List to hold the mapped slot objects
        List<ExaminerSlotDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind parameters using the caller-provided lambda
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                // Map each result row to an ExaminerSlotDTO
                while (rs.next()) {
                    list.add(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return list;
    }

    // Maps a result-set row to an {@link ExaminerSlotDTO}.
    private ExaminerSlotDTO mapSlot(ResultSet rs) throws SQLException {
        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setExamId(rs.getInt("examId"));
        slot.setExaminerUserId(rs.getInt("examinerUserId"));
        // Map the session and examiner display fields
        slot.setExamName(rs.getString("examName"));
        slot.setExaminerUsername(rs.getString("examinerUsername"));
        String examinerName = rs.getString("examinerName");
        if (examinerName == null || examinerName.isBlank()) {
            examinerName = rs.getString("examinerUsername");
        }
        slot.setExaminerName(examinerName);

        // Attempt to read the area ID from the ExamArea LEFT JOIN
        int areaId = rs.getInt("areaId");
        if (!rs.wasNull()) {
            // Area was found via the join — use the direct values
            slot.setAreaId(areaId);
            slot.setAreaName(rs.getString("areaName"));
            slot.setAreaType(rs.getString("areaType"));
        } else {
            // Area join returned null — fall back to parsing the audit mapping entity ID
            String mappingEntityId = rs.getString("mappingEntityId");
            int[] parsed = parseMappingEntityId(mappingEntityId);
            if (parsed != null) {
                // Extract the area ID from the middle component of the entity ID
                slot.setAreaId(parsed[1]);
            }
        }

        // Map the exam type ID (1=Theory, 2=Practical, 4=Road)
        int examTypeId = rs.getInt("examTypeId");
        slot.setExamTypeId(examTypeId);
        // Resolve the exam type name from the query or fall back to a Vietnamese constant
        String examTypeName = rs.getString("examTypeName");
        slot.setExamTypeName(examTypeName != null && !examTypeName.isBlank()
                ? examTypeName
                : examTypeFromId(examTypeId));
        return slot;
    }

    // Builds the audit entity ID as {@code examId:areaId:examinerId}.
    static String buildMappingEntityId(int examId, int areaId, int examinerId) {
        return examId + ":" + areaId + ":" + examinerId;
    }

    // Delegates to {@link #parseSlotKey}.
    static int[] parseMappingEntityId(String entityId) {
        return parseSlotKey(entityId);
    }

    // Converts an exam-type ID to its constant name.
    static String examTypeFromId(int examTypeId) {
        return switch (examTypeId) {
            case 2 ->
                examstaff.enums.ExamSection.THUC_HANH_TRONG_HINH.getDisplayName();
            case 4 ->
                examstaff.enums.ExamSection.THUC_HANH_TREN_DUONG.getDisplayName();
            default ->
                examstaff.enums.ExamSection.LY_THUYET.getDisplayName();
        };
    }

    // Parses a colon-delimited slot key into {@code [examId, areaId, examinerId]}.
    static int[] parseSlotKey(String slotKey) {
        // Return null for null input
        if (slotKey == null) {
            return null;
        }
        // Split on colon delimiter
        String[] parts = slotKey.split(":");
        // Validate that exactly three components exist
        if (parts.length != 3) {
            return null;
        }
        try {
            // Parse each component to an integer
            return new int[]{
                Integer.parseInt(parts[0]), // examId
                Integer.parseInt(parts[1]), // areaId
                Integer.parseInt(parts[2]) // examinerId
            };
        } catch (NumberFormatException e) {
            // Return null if any component is not a valid integer
            return null;
        }
    }

    // Maps a result-set row to a {@link UserDTO} with nested {@link Profile}.
    private UserDTO mapExaminer(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO();
        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));

        Integer profileId = (Integer) rs.getObject("ProfileId");
        if (profileId != null) {
            Profile profile = new Profile();
            profile.setId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            Date dob = rs.getDate("DateOfBirth");
            profile.setDateOfBirth(dob != null ? new java.sql.Timestamp(dob.getTime()) : null);
            profile.setPhoneNumber(rs.getString("PhoneNumber"));
            profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setSex(rs.getBoolean("Sex"));
            user.setProfile(profile);
        }

        return user;
    }

    // Functional interface for parameter binding on prepared statements.
    @FunctionalInterface
    private interface SqlBinder {

        void bind(PreparedStatement ps) throws SQLException;
    }
}


