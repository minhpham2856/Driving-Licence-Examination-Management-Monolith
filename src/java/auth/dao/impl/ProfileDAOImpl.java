package auth.dao.impl;

import java.util.*;
import shared.dbconnection.DBContext;
import auth.dao.ProfileDAO;
import shared.model.Profile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileDAOImpl extends DBContext implements ProfileDAO {

    private static final Logger LOG = Logger.getLogger(ProfileDAOImpl.class.getName());
    private static final String PROFILE_SELECT = """
                     select ProfileId, FullName, DateOfBirth, PhoneNumber, Sex,
                            GovernmentIdNumber, Address, UserId
                     from Profile
                     """;

    @Override
    public Profile getById(int profileId) {
        if (profileId <= 0) {
            return null;
        }
        String sql = PROFILE_SELECT + " where ProfileId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to find profile by id: {0}", e.getMessage());
        }
        return null;
    }

    @Override
    public Profile getByUserId(int userId) {
        if (userId <= 0) {
            return null;
        }
        String sql = PROFILE_SELECT + " where UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to find profile by user id: {0}", e.getMessage());
        }
        return null;
    }

    @Override
    public Profile getByGovIdNo(String govIdNo) {
        String sql = PROFILE_SELECT + " where GovernmentIdNumber = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, govIdNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to find profile by gov id: {0}", e.getMessage());
        }
        return null;
    }

    @Override
    public Profile getByPhoneNo(String phoneNo) {
        String sql = PROFILE_SELECT + " where PhoneNumber = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, phoneNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to find profile by phone: {0}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean insert(Profile profile) {
        Connection conn = getConnection();
        if (conn == null) {
            LOG.severe("Cannot insert profile: database connection is unavailable.");
            return false;
        }
        String sql = """
                     insert into Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
                     values (?, ?, ?, ?, ?, ?, ?)
                     """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, profile.getFullName());
            ps.setTimestamp(2, profile.getDateOfBirth());
            ps.setString(3, profile.getPhoneNumber());
            ps.setBoolean(4, profile.isSex());
            ps.setString(5, profile.getGovernmentIdNumber());
            if (profile.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, profile.getAddress());
            }
            ps.setInt(7, profile.getUserId());
            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setProfileId(generatedKeys.getInt(1));
                }
            }
            return profile.getProfileId() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to insert profile for user {0}: {1}",
                    new Object[]{profile.getUserId(), e.getMessage()});
        }
        return false;
    }

    @Override
    public boolean update(Profile profile) {
        String sql = """
                     update Profile
                     set FullName = ?, DateOfBirth = ?, PhoneNumber = ?, Sex = ?,
                         GovernmentIdNumber = ?, Address = ?
                     where ProfileId = ?
                     """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, profile.getFullName());
            ps.setTimestamp(2, profile.getDateOfBirth());
            ps.setString(3, profile.getPhoneNumber());
            ps.setBoolean(4, profile.isSex());
            ps.setString(5, profile.getGovernmentIdNumber());
            if (profile.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, profile.getAddress());
            }
            ps.setInt(7, profile.getProfileId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to update profile {0}: {1}",
                    new Object[]{profile.getProfileId(), e.getMessage()});
        }
        return false;
    }

    private Profile mapResultSet(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setProfileId(rs.getInt("ProfileId"));
        profile.setUserId(rs.getInt("UserId"));
        profile.setFullName(rs.getString("FullName"));
        profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        profile.setPhoneNumber(rs.getString("PhoneNumber"));
        profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        profile.setAddress(rs.getString("Address"));
        profile.setSex(rs.getBoolean("Sex"));
        return profile;
    }

    @Override
    public List<Profile> getAllByUserIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < userIds.size(); i++) {
            placeholders.append("?");
            if (i < userIds.size() - 1) {
                placeholders.append(",");
            }
        }
        String sql = PROFILE_SELECT + " WHERE UserId IN (" + placeholders.toString() + ")";
        List<Profile> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < userIds.size(); i++) {
                ps.setInt(i + 1, userIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

