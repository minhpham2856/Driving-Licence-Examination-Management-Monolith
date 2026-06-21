package DAOs.Impl;

import Utils.ExamConstants;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DBConnection.DBContext;
import DAOs.ExaminerAssignmentDAO;
import Models.Profile;
import Models.User;
import DTOs.UserDTO;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

 // JDBC implementation of {@link ExaminerAssignmentDAO}.
public class ExaminerAssignmentDAOImpl extends DBContext implements ExaminerAssignmentDAO {

    // Entity name used in the Audit table for examiner-area mappings.
    private static final String ROOM_MAPPING_ENTITY = "Session_ExaminerArea";

    // SQL to list all active examiners with their profile and role info.
    private static final String EXAMINER_SELECT = """
            SELECT u.UserId,
                   u.Username,
                   u.Email,
                   u.PasswordHash,
                   r.RoleName AS [Role],
                   u.[Status],
                   p.ProfileId,
                   p.FullName,
                   p.DateOfBirth,
                   p.PhoneNumber,
                   p.Sex,
                   p.GovernmentIdNumber,
                   p.Address
            FROM [User] u
            JOIN [Role] r ON r.RoleId = u.RoleId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            WHERE r.RoleName = 'Examiner' AND u.[Status] = 1
            ORDER BY p.FullName, u.Username
            """;

    // SQL core used to build slot-queries; always appended with a WHERE clause.
    private static final String SLOT_SELECT = """
            SELECT se.SessionExaminerId,
                   se.SessionId AS examSessionId,
                   se.ExaminerId AS examinerUserId,
                   s.SessionName AS sessionName,
                   eu.Username AS examinerUsername,
                   ep.FullName AS examinerName,
                   roomMap.mappingEntityId,
                   ea.ExamAreaId AS areaId,
                   ea.AreaName AS areaName,
                   ea.AreaType AS areaType,
                   CASE
                       WHEN sect.examTypeName LIKE N'%Thuc hanh%' OR sect.examTypeName LIKE '%Practical%' THEN 2
                       WHEN sect.examTypeName LIKE N'%Duong%' OR sect.examTypeName LIKE '%Road%' THEN 4
                       ELSE 1
                   END AS examTypeId,
                   sect.examTypeName
            FROM Session_Examiner se
            JOIN [Session] s ON s.SessionId = se.SessionId
            JOIN [User] eu ON eu.UserId = se.ExaminerId
            JOIN Profile ep ON ep.UserId = eu.UserId
            OUTER APPLY (
                SELECT TOP 1 a.EntityId AS mappingEntityId
                FROM Audit a
                WHERE a.EntityName = 'Session_ExaminerArea'
                  AND a.EntityId LIKE CAST(se.SessionId AS VARCHAR(20)) + ':%:' + CAST(se.ExaminerId AS VARCHAR(20))
                ORDER BY a.CreatedAt DESC
            ) roomMap
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = TRY_CAST(
                PARSENAME(REPLACE(roomMap.mappingEntityId, ':', '.'), 2) AS INT)
            LEFT JOIN (
                SELECT ses.SessionId,
                       MIN(es.SectionName) AS examTypeName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                GROUP BY ses.SessionId
            ) sect ON sect.SessionId = s.SessionId
            """;

    // Retrieves all active examiner users with their profiles from the database
    @Override
    public List<UserDTO> getActiveExaminers() {
        // List to hold the examiner DTOs
        List<UserDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(EXAMINER_SELECT);
                ResultSet rs = ps.executeQuery()) {
            // Map each result row to a UserDTO with nested Profile
            while (rs.next()) {
                list.add(mapExaminer(rs));
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return list;
    }

         // Inserts a Session_Examiner row and an audit mapping in a single transaction.
    @Override
    public boolean assign(ExaminerSlot slot) {
        // Build the composite entity ID for the audit mapping (sessionId:areaId:examinerId)
        String entityId = buildMappingEntityId(slot.getExamSessionId(), slot.getAreaId(), slot.getExaminerUserId());
        // SQL: insert the examiner assignment into Session_Examiner
        String insertAssignment = """
                INSERT INTO Session_Examiner (SessionId, ExaminerId)
                VALUES (?, ?)
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
            // Step 1: insert the Session_Examiner row
            try (PreparedStatement ps = getConnection().prepareStatement(insertAssignment)) {
                ps.setInt(1, slot.getExamSessionId());
                ps.setInt(2, slot.getExaminerUserId());
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

         // Removes a Session_Examiner row and its audit mapping in a single transaction.
    @Override
    public boolean remove(String slotKey) {
        // Parse the composite slot key into its three integer components
        int[] parts = parseSlotKey(slotKey);
        // Return false if the key is malformed (null or wrong number of parts)
        if (parts == null) {
            return false;
        }
        // Extract the individual components from the parsed array
        int sessionId = parts[0];
        int areaId = parts[1];
        int examinerId = parts[2];
        // Rebuild the audit entity ID from the parsed components
        String entityId = buildMappingEntityId(sessionId, areaId, examinerId);
        // SQL: delete the Session_Examiner row
        String deleteAssignment = "DELETE FROM Session_Examiner WHERE SessionId = ? AND ExaminerId = ?";
        // SQL: delete the audit mapping row
        String deleteMapping = "DELETE FROM Audit WHERE EntityName = ? AND EntityId = ?";
        try {
            // Begin transaction — both deletions must succeed together
            getConnection().setAutoCommit(false);
            // Step 1: delete the Session_Examiner row
            try (PreparedStatement ps = getConnection().prepareStatement(deleteAssignment)) {
                ps.setInt(1, sessionId);
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

    // Retrieves all assignment slots for a specific session, ordered by area name
    @Override
    public List<ExaminerSlot> getBySessionId(int sessionId) {
        // Append WHERE clause to filter by session ID
        String sql = SLOT_SELECT + " WHERE se.SessionId = ? ORDER BY ea.AreaName, se.SessionExaminerId";
        return querySlots(sql, ps -> ps.setInt(1, sessionId));
    }

    // Retrieves all in-progress assignments for a specific examiner (with valid area mappings)
    @Override
    public List<ExaminerSlot> getInProgressAssignmentsForExaminer(int examinerUserId) {
        // Append WHERE clause to filter by examiner and in-progress session status
        String sql = SLOT_SELECT
                + " WHERE se.ExaminerId = ? AND s.[Status] = 'InProgress'"
                + " ORDER BY se.SessionId DESC, se.SessionExaminerId";
        // Execute the query
        List<ExaminerSlot> slots = querySlots(sql, ps -> ps.setInt(1, examinerUserId));
        // Filter out slots without a valid area mapping (legacy or incomplete assignments)
        List<ExaminerSlot> withArea = new ArrayList<>();
        for (ExaminerSlot slot : slots) {
            // Only include slots where the area ID was successfully resolved
            if (slot.getAreaId() > 0) {
                withArea.add(slot);
            }
        }
        return withArea;
    }

    // Retrieves all assignment slots for sessions occurring on a specific exam date
    @Override
    public List<ExaminerSlot> getByExamDate(Date examDate, Map<Integer, Date> sessionDates) {
        // Return empty list if no exam date is provided
        if (examDate == null) {
            return List.of();
        }
        // Find all session IDs that match the target exam date
        List<Integer> sessionIds = new ArrayList<>();
        for (Map.Entry<Integer, Date> e : sessionDates.entrySet()) {
            if (examDate.equals(e.getValue())) {
                sessionIds.add(e.getKey());
            }
        }
        // Return empty list if no sessions fall on this date
        if (sessionIds.isEmpty()) {
            return List.of();
        }
        // Build a dynamic IN clause with the correct number of placeholders
        StringBuilder sql = new StringBuilder(SLOT_SELECT + " WHERE se.SessionId IN (");
        for (int i = 0; i < sessionIds.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(") ORDER BY se.SessionId, ea.AreaName, se.SessionExaminerId");
        // Execute the query, binding each session ID as a positional parameter
        return querySlots(sql.toString(), ps -> {
            for (int i = 0; i < sessionIds.size(); i++) {
                ps.setInt(i + 1, sessionIds.get(i));
            }
        });
    }

    // Retrieves the set of examiner user IDs who have assignments on a given exam date
    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        // Collect all examiner IDs from the date-scoped slot query
        Set<Integer> busy = new HashSet<>();
        for (ExaminerSlot slot : getByExamDate(examDate, sessionDates)) {
            busy.add(slot.getExaminerUserId());
        }
        return busy;
    }

         // Executes a parameterised slot query and maps result rows to ExaminerSlot objects.
    private List<ExaminerSlot> querySlots(String sql, SqlBinder binder) {
        // List to hold the mapped slot objects
        List<ExaminerSlot> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind parameters using the caller-provided lambda
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                // Map each result row to an ExaminerSlot
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

         // Maps a result-set row to an {@link ExaminerSlot}.
    private ExaminerSlot mapSlot(ResultSet rs) throws SQLException {
        ExaminerSlot slot = new ExaminerSlot();
        // Map the primary Session_Examiner fields
        slot.setSessionExaminerId(rs.getInt("SessionExaminerId"));
        slot.setExamSessionId(rs.getInt("examSessionId"));
        slot.setExaminerUserId(rs.getInt("examinerUserId"));
        // Map the session and examiner display fields
        slot.setSessionName(rs.getString("sessionName"));
        slot.setExaminerUsername(rs.getString("examinerUsername"));
        slot.setExaminerName(rs.getString("examinerName"));

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
                : ExamConstants.examTypeToVietnamese(examTypeFromId(examTypeId)));
        return slot;
    }

    // Builds the audit entity ID as {@code sessionId:areaId:examinerId}.
    static String buildMappingEntityId(int sessionId, int areaId, int examinerId) {
        return sessionId + ":" + areaId + ":" + examinerId;
    }

    // Delegates to {@link #parseSlotKey}.
    static int[] parseMappingEntityId(String entityId) {
        return parseSlotKey(entityId);
    }

    // Converts an exam-type ID to its constant name.
    static String examTypeFromId(int examTypeId) {
        return switch (examTypeId) {
            case 2 -> ExamConstants.EXAM_PRACTICAL;  // Practical exam type
            case 4 -> ExamConstants.EXAM_ON_ROAD;    // On-road exam type
            default -> ExamConstants.EXAM_THEORY;    // Theory exam type (default)
        };
    }

         // Parses a colon-delimited slot key into {@code [sessionId, areaId, examinerId]}.
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
                Integer.parseInt(parts[0]),  // sessionId
                Integer.parseInt(parts[1]),  // areaId
                Integer.parseInt(parts[2])   // examinerId
            };
        } catch (NumberFormatException e) {
            // Return null if any component is not a valid integer
            return null;
        }
    }

    // Maps a result-set row to a {@link UserDTO} with nested {@link Profile}.
    private UserDTO mapExaminer(ResultSet rs) throws SQLException {
        // Create and populate the user DTO
        UserDTO user = new UserDTO();
        user.setId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        // Map the Status column to the isActive boolean
        user.setIsActive(rs.getBoolean("Status"));

        // Resolve the role from the RoleName string
        String roleName = rs.getString("Role");
        user.setRole(ExamConstants.roleFromName(roleName));
        user.setRoleId(ExamConstants.roleIdFromName(roleName));

        // Check if a Profile record exists for this user (LEFT JOIN may return null)
        Integer profileId = (Integer) rs.getObject("ProfileId");
        if (profileId != null) {
            // Profile exists — create and populate the nested Profile object
            Profile profile = new Profile();
            profile.setId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            // Convert SQL Date to Timestamp for the DOB field
            Date dob = rs.getDate("DateOfBirth");
            profile.setDateOfBirth(dob != null ? new java.sql.Timestamp(dob.getTime()) : null);
            profile.setPhoneNumber(rs.getString("PhoneNumber"));
            profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setSex(rs.getString("Sex"));
            // Attach the profile to the user DTO
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
