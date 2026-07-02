package dao.impl;

import dbconnection.DBContext;

import dao.ProfileDAO;

import model.user.Profile;
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
    public Profile getById(int id) {
        String sql = PROFILE_SELECT + " where ProfileId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to load profile {0}: {1}", new Object[]{id, e.getMessage()});
        }

        return null;
    }

    @Override
    public Profile getByUserId(int userId) {
        String sql = PROFILE_SELECT + " where UserId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to load profile for user {0}: {1}", new Object[]{userId, e.getMessage()});
        }

        return null;
    }

    /**
     * Finds a profile by GovernmentIdNumber (CMND/CCCD).
     *
     * @param govIdNo the government ID to search for
     * @return the matching Profile, or null
     */
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

    /**
     * Finds a profile by phone number.
     *
     * @param phoneNo the phone number to search for
     * @return the matching Profile, or null
     */
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

    /**
     * Inserts a new Profile with RETURN_GENERATED_KEYS and populates the
     * profile ID.
     *
     * @param profile the Profile to insert (id will be set on success)
     * @return true if insertion succeeded
     */
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
            ps.setString(3, profile.getPhoneNo());
            ps.setString(4, enums.Gender.sexFromGender(profile.isGender()));
            ps.setString(5, profile.getGovIdNo());

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
                    profile.setId(generatedKeys.getInt(1));
                }
            }

            return profile.getId() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to insert profile for user {0}: {1}",
                    new Object[]{profile.getUserId(), e.getMessage()});
        }

        return false;
    }

    /**
     * Updates all mutable fields of an existing Profile.
     *
     * @param profile the Profile containing updated values
     * @return true if at least one row was updated
     */
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
            ps.setString(3, profile.getPhoneNo());
            ps.setString(4, enums.Gender.sexFromGender(profile.isGender()));
            ps.setString(5, profile.getGovIdNo());

            if (profile.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, profile.getAddress());
            }

            ps.setInt(7, profile.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to update profile {0}: {1}",
                    new Object[]{profile.getId(), e.getMessage()});
        }

        return false;
    }

    /**
     * Maps a ResultSet row into a Profile model with gender conversion.
     */
    private Profile mapResultSet(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setId(rs.getInt("ProfileId"));
        profile.setUserId(rs.getInt("UserId"));
        profile.setFullName(rs.getString("FullName"));
        profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        profile.setPhoneNo(rs.getString("PhoneNumber"));
        profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
        profile.setAddress(rs.getString("Address"));
        profile.setGender(enums.Gender.genderFromSex(rs.getString("Sex")));
        return profile;
    }
}
