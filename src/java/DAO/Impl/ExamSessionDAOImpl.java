package DAO.Impl;

import DAO.ExamSessionDAO;
import DBConnection.DBContext;
import Models.ExamSessionOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamSessionDAOImpl extends DBContext implements ExamSessionDAO {

    private static final String SESSION_SELECT = """
            select es.id,
                   es.sessionName,
                   lt.licenseCode,
                   es.examDate,
                   coalesce(ea.location, ea.areaName) as location,
                   es.maxCandidates,
                   es.registeredCount
            from ExamSession es
            join LicenseType lt on es.licenseTypeId = lt.id
            join ExamArea ea on es.areaId = ea.id
            """;

    @Override
    public List<ExamSessionOption> findOpenByLicenseCode(String licenseCode) {
        String sql = SESSION_SELECT + """
                where lt.licenseCode = ?
                  and es.status in ('Scheduled', 'Open')
                  and es.examDate >= cast(getutcdate() as date)
                  and es.registeredCount < es.maxCandidates
                order by es.examDate asc, es.shiftStartTime asc
                """;

        List<ExamSessionOption> sessions = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, licenseCode);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(mapSession(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    @Override
    public Optional<ExamSessionOption> findById(int sessionId) {
        String sql = SESSION_SELECT + " where es.id = ?";

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sessionId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapSession(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean incrementRegisteredCount(int sessionId) {
        String sql = """
                update ExamSession
                set registeredCount = registeredCount + 1
                where id = ?
                  and registeredCount < maxCandidates
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean hasAvailableSlot(int sessionId) {
        String sql = """
                select 1
                from ExamSession
                where id = ?
                  and status in ('Scheduled', 'Open')
                  and registeredCount < maxCandidates
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sessionId);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ExamSessionOption mapSession(ResultSet rs) throws SQLException {
        ExamSessionOption session = new ExamSessionOption();
        int id = rs.getInt("id");
        int maxCandidates = rs.getInt("maxCandidates");
        int registeredCount = rs.getInt("registeredCount");

        session.setId(id);
        session.setExamName(rs.getString("sessionName"));
        session.setExamCode("SH-" + id);
        session.setLicenceClass(rs.getString("licenseCode"));
        session.setExamDate(rs.getDate("examDate"));
        session.setLocation(rs.getString("location"));
        session.setSlotsRemaining(Math.max(0, maxCandidates - registeredCount));
        return session;
    }
}
