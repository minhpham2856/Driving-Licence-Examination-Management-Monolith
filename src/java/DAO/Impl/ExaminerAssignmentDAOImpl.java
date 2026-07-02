package dao.impl;

import enums.Db2Mappings;
import enums.ExamTypes;
import controller.staff.exam.ExaminerSlot;
import dbconnection.DBContext;
import dao.ExaminerAssignmentDAO;
import model.user.Profile;
import model.user.Role;
import model.user.User;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAssignmentDAOImpl extends DBContext implements ExaminerAssignmentDAO {

    private static final String ROOM_MAPPING_ENTITY = "Session_ExaminerArea";

    private static final String EXAMINER_SELECT = """
            SELECT u.UserId,
                   u.Username,
                   u.Email,
                   u.PasswordHash,
                   u.[Role],
                   u.[Status],
                   p.ProfileId,
                   p.FullName,
                   p.DateOfBirth,
                   p.PhoneNumber,
                   p.Sex,
                   p.GovernmentIdNumber,
                   p.Address
            FROM [User] u
            LEFT JOIN Profile p ON p.UserId = u.UserId
            WHERE u.[Role] = 'Examiner' AND u.[Status] = 1
            ORDER BY p.FullName, u.Username
            """;

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
                       WHEN sect.examTypeName LIKE N'%Thực hành%' OR sect.examTypeName LIKE '%Practical%' THEN 2
                       WHEN sect.examTypeName LIKE N'%Đường%' OR sect.examTypeName LIKE '%Road%' THEN 4
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

    @Override
    public List<User> getActiveExaminers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(EXAMINER_SELECT);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapExaminer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean assign(ExaminerSlot slot) {
        String entityId = buildMappingEntityId(slot.getExamSessionId(), slot.getAreaId(), slot.getExaminerUserId());
        String insertAssignment = """
                INSERT INTO Session_Examiner (SessionId, ExaminerId)
                VALUES (?, ?)
                """;
        String deleteMapping = """
                DELETE FROM Audit
                WHERE EntityName = ? AND EntityId = ?
                """;
        String insertMapping = """
                INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'ASSIGN', ?, ?, ?, GETDATE())
                """;
        try {
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(insertAssignment)) {
                ps.setInt(1, slot.getExamSessionId());
                ps.setInt(2, slot.getExaminerUserId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(insertMapping)) {
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                ps.setInt(1, assignedBy);
                ps.setString(2, ROOM_MAPPING_ENTITY);
                ps.setString(3, entityId);
                ps.setString(4, slot.getAreaName() != null ? slot.getAreaName() : String.valueOf(slot.getAreaId()));
                ps.executeUpdate();
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean remove(String slotKey) {
        int[] parts = parseSlotKey(slotKey);
        if (parts == null) {
            return false;
        }
        int sessionId = parts[0];
        int areaId = parts[1];
        int examinerId = parts[2];
        String entityId = buildMappingEntityId(sessionId, areaId, examinerId);
        String deleteAssignment = "DELETE FROM Session_Examiner WHERE SessionId = ? AND ExaminerId = ?";
        String deleteMapping = "DELETE FROM Audit WHERE EntityName = ? AND EntityId = ?";
        try {
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(deleteAssignment)) {
                ps.setInt(1, sessionId);
                ps.setInt(2, examinerId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public List<ExaminerSlot> getBySessionId(int sessionId) {
        String sql = SLOT_SELECT + " WHERE se.SessionId = ? ORDER BY ea.AreaName, se.SessionExaminerId";
        return querySlots(sql, ps -> ps.setInt(1, sessionId));
    }

    @Override
    public List<ExaminerSlot> getInProgressAssignmentsForExaminer(int examinerUserId) {
        String sql = SLOT_SELECT
                + " WHERE se.ExaminerId = ? AND s.[Status] = 'InProgress'"
                + " ORDER BY se.SessionId DESC, se.SessionExaminerId";
        List<ExaminerSlot> slots = querySlots(sql, ps -> ps.setInt(1, examinerUserId));
        List<ExaminerSlot> withArea = new ArrayList<>();
        for (ExaminerSlot slot : slots) {
            if (slot.getAreaId() > 0) {
                withArea.add(slot);
            }
        }
        return withArea;
    }

    @Override
    public List<ExaminerSlot> getByExamDate(Date examDate, Map<Integer, Date> sessionDates) {
        if (examDate == null) {
            return List.of();
        }
        List<Integer> sessionIds = new ArrayList<>();
        for (Map.Entry<Integer, Date> e : sessionDates.entrySet()) {
            if (examDate.equals(e.getValue())) {
                sessionIds.add(e.getKey());
            }
        }
        if (sessionIds.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder(SLOT_SELECT + " WHERE se.SessionId IN (");
        for (int i = 0; i < sessionIds.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(") ORDER BY se.SessionId, ea.AreaName, se.SessionExaminerId");
        return querySlots(sql.toString(), ps -> {
            for (int i = 0; i < sessionIds.size(); i++) {
                ps.setInt(i + 1, sessionIds.get(i));
            }
        });
    }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) {
        Set<Integer> busy = new HashSet<>();
        for (ExaminerSlot slot : getByExamDate(examDate, sessionDates)) {
            busy.add(slot.getExaminerUserId());
        }
        return busy;
    }

    private List<ExaminerSlot> querySlots(String sql, SqlBinder binder) {
        List<ExaminerSlot> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExaminerSlot mapSlot(ResultSet rs) throws SQLException {
        ExaminerSlot slot = new ExaminerSlot();
        slot.setSessionExaminerId(rs.getInt("SessionExaminerId"));
        slot.setExamSessionId(rs.getInt("examSessionId"));
        slot.setExaminerUserId(rs.getInt("examinerUserId"));
        slot.setSessionName(rs.getString("sessionName"));
        slot.setExaminerUsername(rs.getString("examinerUsername"));
        slot.setExaminerName(rs.getString("examinerName"));

        int areaId = rs.getInt("areaId");
        if (!rs.wasNull()) {
            slot.setAreaId(areaId);
            slot.setAreaName(rs.getString("areaName"));
            slot.setAreaType(rs.getString("areaType"));
        } else {
            String mappingEntityId = rs.getString("mappingEntityId");
            int[] parsed = parseMappingEntityId(mappingEntityId);
            if (parsed != null) {
                slot.setAreaId(parsed[1]);
            }
        }

        int examTypeId = rs.getInt("examTypeId");
        slot.setExamTypeId(examTypeId);
        String examTypeName = rs.getString("examTypeName");
        slot.setExamTypeName(examTypeName != null && !examTypeName.isBlank()
                ? examTypeName
                : ExamTypes.toVietnamese(examTypeFromId(examTypeId)));
        return slot;
    }

    static String buildMappingEntityId(int sessionId, int areaId, int examinerId) {
        return sessionId + ":" + areaId + ":" + examinerId;
    }

    static int[] parseMappingEntityId(String entityId) {
        return parseSlotKey(entityId);
    }

    static String examTypeFromId(int examTypeId) {
        return switch (examTypeId) {
            case 2 -> ExamTypes.PRACTICAL;
            case 4 -> ExamTypes.ON_ROAD;
            default -> ExamTypes.THEORY;
        };
    }

    static int[] parseSlotKey(String slotKey) {
        if (slotKey == null) {
            return null;
        }
        String[] parts = slotKey.split(":");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private User mapExaminer(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setIsActive(rs.getBoolean("Status"));

        Integer profileId = (Integer) rs.getObject("ProfileId");
        user.setProfileId(profileId);

        String roleName = rs.getString("Role");
        Role role = Db2Mappings.roleFromName(roleName);
        user.setRole(role);
        user.setRoleId(role.getId());

        if (profileId != null) {
            Profile profile = new Profile();
            profile.setId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            profile.setDateOfBirth(rs.getDate("DateOfBirth"));
            profile.setPhoneNo(rs.getString("PhoneNumber"));
            profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setGender(Db2Mappings.genderFromSex(rs.getString("Sex")));
            user.setProfile(profile);
        }

        return user;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
