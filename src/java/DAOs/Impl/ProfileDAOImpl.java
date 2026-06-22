package DAOs.Impl;

import Utils.ExamConstants;
import DBConnection.DBContext;
import DAOs.ProfileDAO;
import Models.Profile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileDAOImpl implements ProfileDAO {

    private final DBContext ctx;
    private static final String PROFILE_SELECT = """
                     select ProfileId, FullName, DateOfBirth, PhoneNumber, Sex,
                            GovernmentIdNumber, Address, UserId
                     from Profile
                     """;

    public ProfileDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public Profile getById(int id) {
        String sql = PROFILE_SELECT + " where ProfileId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Profile getByGovIdNo(String govIdNo) {
        String sql = PROFILE_SELECT + " where GovernmentIdNumber = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, govIdNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Profile getByPhoneNo(String phoneNo) {
        String sql = PROFILE_SELECT + " where PhoneNumber = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, phoneNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToProfile(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Profile profile) {
        Connection conn = ctx.getConnection();
        if (conn == null) {
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
            ps.setString(4, ExamConstants.sexFromGender(profile.isGender()));
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
                    profile.setProfileId(generatedKeys.getInt(1));
                }
            }

            return profile.getProfileId() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, profile.getFullName());
            ps.setTimestamp(2, profile.getDateOfBirth());
            ps.setString(3, profile.getPhoneNo());
            ps.setString(4, ExamConstants.sexFromGender(profile.isGender()));
            ps.setString(5, profile.getGovIdNo());

            if (profile.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, profile.getAddress());
            }

            ps.setInt(7, profile.getProfileId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Maps ResultSet row into Profile
    private Profile mapToProfile(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setProfileId(rs.getInt("ProfileId"));
        profile.setUserId(rs.getInt("UserId"));
        profile.setFullName(rs.getString("FullName"));
        profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        profile.setPhoneNo(rs.getString("PhoneNumber"));
        profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
        profile.setAddress(rs.getString("Address"));
        profile.setGender(ExamConstants.genderFromSex(rs.getString("Sex")));
        return profile;
    }
}
