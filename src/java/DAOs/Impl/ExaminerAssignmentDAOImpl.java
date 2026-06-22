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

public class ExaminerAssignmentDAOImpl implements ExaminerAssignmentDAO {

    private final DBContext ctx;

    public ExaminerAssignmentDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<UserDTO> getActiveExaminers() {
        List<UserDTO> list = new ArrayList<>();

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(EXAMINER_SELECT);
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
                insert into Session_Examiner (SessionId, ExaminerId)
                values (?, ?)
                """;
        String deleteMapping = """
                delete from Audit
                where EntityName = ? and EntityId = ?
                """;
        String insertMapping = """
                insert into Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt)
                values (?, 'ASSIGN', ?, ?, ?, GETDATE())
                """;

        try {
            ctx.getConnection().setAutoCommit(false);

            try (PreparedStatement ps = ctx.getConnection().prepareStatement(insertAssignment)) {
                ps.setInt(1, slot.getExamSessionId());
                ps.setInt(2, slot.getExaminerUserId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = ctx.getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = ctx.getConnection().prepareStatement(insertMapping)) {
                int assignedBy = slot.getAssignedBy() > 0 ? slot.getAssignedBy() : 3;
                ps.setInt(1, assignedBy);
                ps.setString(2, ROOM_MAPPING_ENTITY);
                ps.setString(3, entityId);
                ps.setString(4, slot.getAreaName() != null ? slot.getAreaName() : String.valueOf(slot.getAreaId()));
                ps.executeUpdate();
            }

            ctx.getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                ctx.getConnection().rollback();
            } catch (SQLException ignored) {
            }

            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                return false;
            }

            e.printStackTrace();
        } finally {
            try {
                ctx.getConnection().setAutoCommit(true);
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
        String deleteAssignment = "delete from Session_Examiner where SessionId = ? and ExaminerId = ?";
        String deleteMapping = "delete from Audit where EntityName = ? and EntityId = ?";

        try {
            ctx.getConnection().setAutoCommit(false);

            try (PreparedStatement ps = ctx.getConnection().prepareStatement(deleteAssignment)) {
                ps.setInt(1, sessionId);
                ps.setInt(2, examinerId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = ctx.getConnection().prepareStatement(deleteMapping)) {
                ps.setString(1, ROOM_MAPPING_ENTITY);
                ps.setString(2, entityId);
                ps.executeUpdate();
            }

            ctx.getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                ctx.getConnection().rollback();
            } catch (SQLException ignored) {
            }

            e.printStackTrace();
        } finally {
            try {
                ctx.getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }

        return false;
    }

    @Override
    public List<ExaminerSlot> getBySessionId(int sessionId) {
        String sql = SLOT_SELECT + " where se.SessionId = ? order by ea.AreaName, se.SessionExaminerId";
        return querySlots(sql, ps -> ps.setInt(1, sessionId));
    }

    @Override
    public List<ExaminerSlot> getInProgressAssignmentsForExaminer(int examinerUserId) {
        String sql = SLOT_SELECT + """
                where se.ExaminerId = ? and s.[Status] = 'InProgress'
                order by se.SessionId desc, se.SessionExaminerId
                """;

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

        StringBuilder sql = new StringBuilder(SLOT_SELECT + " where se.SessionId in (");
        for (int i = 0; i < sessionIds.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(") order by se.SessionId, ea.AreaName, se.SessionExaminerId");

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

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
                : ExamConstants.examTypeToVietnamese(examTypeFromId(examTypeId)));
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
            case 2 -> ExamConstants.EXAM_PRACTICAL;
            case 4 -> ExamConstants.EXAM_ON_ROAD;
            default -> ExamConstants.EXAM_THEORY;
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

    private UserDTO mapExaminer(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO();
        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setStatus(rs.getBoolean("Status"));

        String roleName = rs.getString("Role");
        user.setRole(ExamConstants.roleFromName(roleName));
        user.setRoleId(ExamConstants.roleIdFromName(roleName));

        Integer profileId = (Integer) rs.getObject("ProfileId");
        if (profileId != null) {
            Profile profile = new Profile();
            profile.setProfileId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            Date dob = rs.getDate("DateOfBirth");
            profile.setDateOfBirth(dob != null ? new java.sql.Timestamp(dob.getTime()) : null);
            profile.setPhoneNumber(rs.getString("PhoneNumber"));
            profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setSex(rs.getString("Sex"));
            user.setProfile(profile);
        }

        return user;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private static final String ROOM_MAPPING_ENTITY = "Session_ExaminerArea";

    private static final String EXAMINER_SELECT = """
            select u.UserId,
                   u.Username,
                   u.Email,
                   u.PasswordHash,
                   r.RoleName as [Role],
                   u.[Status],
                   p.ProfileId,
                   p.FullName,
                   p.DateOfBirth,
                   p.PhoneNumber,
                   p.Sex,
                   p.GovernmentIdNumber,
                   p.Address
            from [User] u
            join [Role] r on r.RoleId = u.RoleId
            left join Profile p on p.UserId = u.UserId
            where r.RoleName = 'Examiner' and u.[Status] = 1
            order by p.FullName, u.Username
            """;

    private static final String SLOT_SELECT = """
            select se.SessionExaminerId,
                   se.SessionId as examSessionId,
                   se.ExaminerId as examinerUserId,
                   s.SessionName as sessionName,
                   eu.Username as examinerUsername,
                   ep.FullName as examinerName,
                   roomMap.mappingEntityId,
                   ea.ExamAreaId as areaId,
                   ea.AreaName as areaName,
                   ea.AreaType as areaType,
                   case
                       when sect.examTypeName like N'%Thuc hanh%' or sect.examTypeName like '%Practical%' then 2
                       when sect.examTypeName like N'%Duong%' or sect.examTypeName like '%Road%' then 4
                       else 1
                   end as examTypeId,
                   sect.examTypeName
            from Session_Examiner se
            join [Session] s on s.SessionId = se.SessionId
            join [User] eu on eu.UserId = se.ExaminerId
            join Profile ep on ep.UserId = eu.UserId
            outer apply (
                select top 1 a.EntityId as mappingEntityId
                from Audit a
                where a.EntityName = 'Session_ExaminerArea'
                  and a.EntityId like CAST(se.SessionId as VARCHAR(20)) + ':%:' + CAST(se.ExaminerId as VARCHAR(20))
                order by a.CreatedAt desc
            ) roomMap
            left join ExamArea ea on ea.ExamAreaId = TRY_CAST(
                PARSENAME(REPLACE(roomMap.mappingEntityId, ':', '.'), 2) as INT)
            left join (
                select ses.SessionId,
                       MIN(es.SectionName) as examTypeName
                from Session_ExamSection ses
                join ExamSection es on es.ExamSectionId = ses.ExamSectionId
                group by ses.SessionId
            ) sect on sect.SessionId = s.SessionId
            """;
}
