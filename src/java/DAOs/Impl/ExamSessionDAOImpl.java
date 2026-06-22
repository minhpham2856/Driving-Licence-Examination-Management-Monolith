package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamSessionDAO;
import DTOs.SessionDTO;
import Models.Session;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExamSessionDAOImpl implements ExamSessionDAO {

    private final DBContext ctx;

    public ExamSessionDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public Session findById(int id) {
        String sql = "select * from [Session] where SessionId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session s = new Session();
                    s.setSessionId(rs.getInt("SessionId"));
                    s.setSessionName(rs.getString("SessionName"));
                    s.setStartTime(rs.getTimestamp("StartTime"));
                    s.setEndTime(rs.getTimestamp("EndTime"));
                    s.setStatus(rs.getString("Status"));
                    s.setExamId(rs.getInt("ExamId"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SessionDTO getById(int id) {
        String sql = SESSION_SELECT + " where s.SessionId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<SessionDTO> getActiveSessions() {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT + """
                where s.[Status] in ('Scheduled', 'Open', 'InProgress')
                order by CAST(s.StartTime as DATE), CAST(s.StartTime as TIME)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<SessionDTO> getAllSessions() {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT + """
                order by CAST(s.StartTime as DATE) desc, CAST(s.StartTime as TIME) desc
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<SessionDTO> getSessionsByExamDate(Date examDate) {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT + " where CAST(s.StartTime as DATE) = ? order by CAST(s.StartTime as TIME)";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setDate(1, examDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExamSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean updateStatus(int sessionId, String status) {
        String sql = "update [Session] set [Status] = ? where SessionId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private SessionDTO mapResultSetToExamSession(ResultSet rs) throws SQLException {
        SessionDTO es = new SessionDTO();
        es.setId(rs.getInt("id"));
        es.setSessionName(rs.getString("sessionName"));
        es.setLicenseTypeId(rs.getInt("licenseTypeId"));
        es.setExamTypeId(rs.getInt("examTypeId"));
        es.setExamDate(rs.getDate("examDate"));
        es.setShiftStartTime(rs.getTime("shiftStartTime"));
        es.setShiftEndTime(rs.getTime("shiftEndTime"));
        es.setAreaId(rs.getInt("areaId"));
        es.setStatus(rs.getString("status"));
        es.setMaxCandidates(rs.getInt("maxCandidates"));
        es.setRegisteredCount(rs.getInt("registeredCount"));
        Timestamp created = rs.getTimestamp("createdAt");
        es.setCreatedAt(rs.wasNull() ? null : created);
        es.setLicenseCode(rs.getString("licenseCode"));
        es.setExamTypeName(rs.getString("examTypeName"));
        es.setAreaName(rs.getString("areaName"));
        return es;
    }

    private static final String SESSION_SELECT = """
            select s.SessionId as id,
                   s.SessionName as sessionName,
                   e.LicenceId as licenseTypeId,
                   ISNULL(sect.examTypeId, 1) as examTypeId,
                   CAST(s.StartTime as DATE) as examDate,
                   CAST(s.StartTime as TIME) as shiftStartTime,
                   CAST(s.EndTime as TIME) as shiftEndTime,
                   ISNULL(sea.ExamAreaId, 0) as areaId,
                   s.[Status] as status,
                   ISNULL(ea.Capacity, 100) as maxCandidates,
                   (select count(*) from ExamEnrollment ec2 where ec2.SessionId = s.SessionId) as registeredCount,
                   s.StartTime as createdAt,
                   l.LicenceClass as licenseCode,
                   sect.examTypeName,
                   ea.AreaName as areaName
            from [Session] s
            join Exam e on e.ExamId = s.ExamId
            join Licence l on l.LicenceId = e.LicenceId
            left join (
                select ses.SessionId, MIN(sea2.ExamAreaId) as ExamAreaId
                from Session_ExamArea sea2
                join [Session] ses on ses.SessionId = sea2.SessionId
                group by ses.SessionId
            ) sea on sea.SessionId = s.SessionId
            left join ExamArea ea on ea.ExamAreaId = sea.ExamAreaId
            left join (
                select ses.SessionId,
                       MIN(es.ExamSectionId) as examSectionId,
                       case
                           when MIN(es.SectionName) like N'%Lý thuyết%' or MIN(es.SectionName) like '%Theory%' then 1
                           when MIN(es.SectionName) like N'%Thực hành%' or MIN(es.SectionName) like '%Practical%' then 2
                           when MIN(es.SectionName) like N'%Đường%' or MIN(es.SectionName) like '%Road%' then 4
                           else 1
                       end as examTypeId,
                       MIN(es.SectionName) as examTypeName
                from Session_ExamSection ses
                join ExamSection es on es.ExamSectionId = ses.ExamSectionId
                group by ses.SessionId
            ) sect on sect.SessionId = s.SessionId
            """;
}
