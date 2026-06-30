package dao.impl;

import dao.ExaminerScheduleDAO;
import dbconnection.DBContext;
import dto.ExaminerSlotDTO;
import dto.UserDTO;
import model.ExaminerSchedule;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerScheduleDAOImpl extends DBContext implements ExaminerScheduleDAO {

    private static final String IN_PROGRESS_ASSIGNMENTS_SQL = """
            SELECT es.ExaminerScheduleId,
                   es.SessionId,
                   ISNULL(es.ExamAreaId, 0) AS ExamAreaId,
                   es.ExamSectionId,
                   es.ExaminerId,
                   ISNULL(es.AssignedBy, 0) AS AssignedBy,
                   u.Username AS examinerUsername,
                   COALESCE(p.FullName, u.Username) AS examinerName,
                   ea.AreaName,
                   ea.AreaType,
                   sec.SectionName AS examTypeName,
                   s.SessionName,
                   CASE
                       WHEN sec.SectionName LIKE N'%Lý thuyết%' THEN 1
                       WHEN sec.SectionName LIKE N'%Sa hình%' OR sec.SectionName LIKE N'%Thực hành%' THEN 2
                       WHEN sec.SectionName LIKE N'%Đường%' THEN 4
                       ELSE 1
                   END AS examTypeId
            FROM ExaminerSchedule es
            INNER JOIN [Session] s ON s.SessionId = es.SessionId
            INNER JOIN [User] u ON u.UserId = es.ExaminerId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            LEFT JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = es.ExamAreaId
            WHERE es.ExaminerId = ?
              AND s.[Status] = N'InProgress'
            ORDER BY s.StartTime, es.ExaminerScheduleId
            """;

    @Override
    public boolean insert(ExaminerSchedule schedule) { return false; }

    @Override
    public boolean delete(int examinerScheduleId) { return false; }

    @Override
    public List<ExaminerSchedule> getBySessionId(int sessionId) { return new ArrayList<>(); }

    @Override
    public List<ExaminerSchedule> getByExaminerId(int examinerId) { return new ArrayList<>(); }

    @Override
    public List<ExaminerSchedule> getBySessionIds(List<Integer> sessionIds) { return new ArrayList<>(); }

    @Override
    public List<UserDTO> getActiveExaminers() { return new ArrayList<>(); }

    @Override
    public List<ExaminerSlotDTO> getByExamDate(Date date, Map<Integer, Date> sessionDates) { return new ArrayList<>(); }

    @Override
    public Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates) { return new HashSet<>(); }

    @Override
    public boolean assign(ExaminerSlotDTO slot) { return false; }

    @Override
    public boolean remove(String slotKey) { return false; }

    @Override
    public List<ExaminerSlotDTO> getInProgressAssignmentsForExaminer(int examinerUserId) {
        List<ExaminerSlotDTO> slots = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(IN_PROGRESS_ASSIGNMENTS_SQL)) {
            ps.setInt(1, examinerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slots.add(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }

    private static ExaminerSlotDTO mapSlot(ResultSet rs) throws SQLException {
        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setSessionExaminerId(rs.getInt("ExaminerScheduleId"));
        slot.setExamSessionId(rs.getInt("SessionId"));
        slot.setAreaId(rs.getInt("ExamAreaId"));
        slot.setExamTypeId(rs.getInt("examTypeId"));
        slot.setExaminerUserId(rs.getInt("ExaminerId"));
        slot.setAssignedBy(rs.getInt("AssignedBy"));
        slot.setExaminerUsername(rs.getString("examinerUsername"));
        slot.setExaminerName(rs.getString("examinerName"));
        slot.setAreaName(rs.getString("AreaName"));
        slot.setAreaType(rs.getString("AreaType"));
        slot.setExamTypeName(rs.getString("examTypeName"));
        slot.setSessionName(rs.getString("SessionName"));
        return slot;
    }
}
