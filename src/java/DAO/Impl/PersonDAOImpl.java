package DAO.Impl;

import DBConnection.DBContext;
import DAO.PersonDAO;
import Models.Person;
import java.sql.*;

public class PersonDAOImpl extends DBContext implements PersonDAO {

    private static final String PROFILE_SELECT = """
                     select ProfileId, FullName, DateOfBirth, PhoneNumber, Sex,
                            GovernmentIdNumber, Address, UserId
                     from Profile
                     """;

    @Override
    public Person getById(int id) {
        String sql = PROFILE_SELECT + " where ProfileId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByEmail(String email) {
        String sql = """
                     select p.ProfileId, p.FullName, p.DateOfBirth, p.PhoneNumber, p.Sex,
                            p.GovernmentIdNumber, p.Address, p.UserId
                     from Profile p
                     join [User] u on u.UserId = p.UserId
                     where u.Email = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Person person = mapResultSetToPerson(rs);
                    person.setEmail(email);
                    return person;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByGovIdNo(String govIdNo) {
        String sql = PROFILE_SELECT + " where GovernmentIdNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, govIdNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByPhoneNo(String phoneNo) {
        String sql = PROFILE_SELECT + " where PhoneNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Person person) {
        String sql = """
                     insert into Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
                     values (?, ?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ProfileId"})) {
            ps.setString(1, person.getFullName());
            ps.setDate(2, person.getDateOfBirth());
            ps.setString(3, person.getPhoneNo());
            ps.setString(4, mapGenderToSex(person.isGender()));
            ps.setString(5, person.getGovIdNo());

            if (person.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, person.getAddress());
            }

            ps.setInt(7, person.getUserId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    person.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Person person) {
        String sql = """
                     update Profile
                     set FullName = ?, DateOfBirth = ?, PhoneNumber = ?, Sex = ?,
                         GovernmentIdNumber = ?, Address = ?
                     where ProfileId = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, person.getFullName());
            ps.setDate(2, person.getDateOfBirth());
            ps.setString(3, person.getPhoneNo());
            ps.setString(4, mapGenderToSex(person.isGender()));
            ps.setString(5, person.getGovIdNo());

            if (person.getAddress() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, person.getAddress());
            }

            ps.setInt(7, person.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        Person person = new Person();

        person.setId(rs.getInt("ProfileId"));
        person.setUserId(rs.getInt("UserId"));
        person.setFullName(rs.getString("FullName"));
        person.setDateOfBirth(rs.getDate("DateOfBirth"));
        person.setPhoneNo(rs.getString("PhoneNumber"));
        person.setGovIdNo(rs.getString("GovernmentIdNumber"));
        person.setAddress(rs.getString("Address"));
        person.setGender(mapSexToGender(rs.getString("Sex")));

        return person;
    }

    private String mapGenderToSex(boolean gender) {
        return gender ? "Nữ" : "Nam";
    }

    private boolean mapSexToGender(String sex) {
        return sex != null && "Nữ".equalsIgnoreCase(sex.trim());
    }
}
