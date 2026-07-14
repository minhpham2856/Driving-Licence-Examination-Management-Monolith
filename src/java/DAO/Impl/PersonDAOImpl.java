package DAO.Impl;

import DAO.PersonDAO;
import DBConnection.DBContext;
import Models.CandidateDTO;
import Models.Person;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

public class PersonDAOImpl extends DBContext implements PersonDAO {

    @Override
    public Person getById(int id) {
        String sql = "SELECT * FROM Person WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        String sql = "SELECT * FROM Person WHERE email = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
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
    public Person getByGovIdNo(String govIdNo) {
        String sql = "SELECT * FROM Person WHERE govIdNo = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
    public boolean insert(Person person) {
        String sql = """
                     INSERT INTO Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, photoUrl, isWalkIn, approvalStatus, rejectionReason)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableString(ps, 1, person.getGovIdNo());
            ps.setString(2, person.getFullName());
            ps.setDate(3, person.getDateOfBirth());
            ps.setBoolean(4, person.isGender());
            ps.setString(5, person.getPhoneNo());
            setNullableString(ps, 6, person.getEmail());
            setNullableString(ps, 7, person.getAddress());
            setNullableString(ps, 8, person.getPhotoUrl());
            ps.setBoolean(9, person.isIsWalkIn());
            ps.setString(10, person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending");
            setNullableString(ps, 11, person.getRejectionReason());

            if (ps.executeUpdate() > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        person.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Person person) {
        String sql = """
                     UPDATE Person
                     SET govIdNo = ?, fullName = ?, dateOfBirth = ?, gender = ?, phoneNo = ?, email = ?, address = ?, photoUrl = ?, isWalkIn = ?, updatedAt = GETUTCDATE(), approvalStatus = ?, rejectionReason = ?
                     WHERE id = ?
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setNullableString(ps, 1, person.getGovIdNo());
            ps.setString(2, person.getFullName());
            ps.setDate(3, person.getDateOfBirth());
            ps.setBoolean(4, person.isGender());
            ps.setString(5, person.getPhoneNo());
            setNullableString(ps, 6, person.getEmail());
            setNullableString(ps, 7, person.getAddress());
            setNullableString(ps, 8, person.getPhotoUrl());
            ps.setBoolean(9, person.isIsWalkIn());
            ps.setString(10, person.getApprovalStatus());
            setNullableString(ps, 11, person.getRejectionReason());
            ps.setInt(12, person.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean insertCandidateList(List<CandidateDTO> listCandidates) {
        java.sql.Connection connection = getConnection();
        if (connection == null) {
            return false;
        }

        try {
            connection.setAutoCommit(false);

            String sqlCheckPerson = "SELECT id FROM Person WHERE govIdNo = ?";
            String sqlInsertPerson = "INSERT INTO Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, isWalkIn, approvalStatus) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlUpdatePerson = "UPDATE Person SET fullName = ?, dateOfBirth = ?, phoneNo = ?, updatedAt = GETUTCDATE(), approvalStatus = ? WHERE id = ?";

            boolean canInsertLegacyRegistration = hasColumn("ExamRegistration", "personId")
                    && hasColumn("ExamRegistration", "candidateNo")
                    && hasColumn("ExamRegistration", "licenseClass")
                    && !isRequiredColumnWithoutDefault("ExamRegistration", "examSessionId");
            String sqlInsertRegistration = "INSERT INTO ExamRegistration (personId, candidateNo, licenseClass) VALUES (?, ?, ?)";

            try (PreparedStatement psCheck = connection.prepareStatement(sqlCheckPerson);
                 PreparedStatement psInsertP = connection.prepareStatement(sqlInsertPerson, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdateP = connection.prepareStatement(sqlUpdatePerson);
                 PreparedStatement psInsertR = canInsertLegacyRegistration ? connection.prepareStatement(sqlInsertRegistration) : null) {

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);

                for (CandidateDTO candidate : listCandidates) {
                    int personId = -1;
                    java.util.Date parsedDate = sdf.parse(candidate.getDob());

                    psCheck.setString(1, candidate.getCccd());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            personId = rs.getInt("id");
                        }
                    }

                    if (personId != -1) {
                        psUpdateP.setString(1, candidate.getName());
                        psUpdateP.setDate(2, new java.sql.Date(parsedDate.getTime()));
                        psUpdateP.setString(3, candidate.getPhone());
                        psUpdateP.setString(4, "Approved");
                        psUpdateP.setInt(5, personId);
                        psUpdateP.executeUpdate();
                    } else {
                        psInsertP.setString(1, candidate.getCccd());
                        psInsertP.setString(2, candidate.getName());
                        psInsertP.setDate(3, new java.sql.Date(parsedDate.getTime()));
                        psInsertP.setBoolean(4, true);
                        psInsertP.setString(5, candidate.getPhone());
                        psInsertP.setBoolean(6, false);
                        psInsertP.setString(7, "Approved");
                        psInsertP.executeUpdate();

                        try (ResultSet gk = psInsertP.getGeneratedKeys()) {
                            if (gk.next()) {
                                personId = gk.getInt(1);
                            }
                        }
                    }

                    if (personId != -1 && canInsertLegacyRegistration) {
                        psInsertR.setInt(1, personId);
                        psInsertR.setString(2, candidate.getSbd());
                        psInsertR.setString(3, candidate.getLicenseClass());
                        psInsertR.executeUpdate();
                    }
                }
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        Person person = new Person();
        person.setId(rs.getInt("id"));
        person.setGovIdNo(rs.getString("govIdNo"));
        person.setFullName(rs.getString("fullName"));
        person.setDateOfBirth(rs.getDate("dateOfBirth"));
        person.setGender(rs.getBoolean("gender"));
        person.setPhoneNo(rs.getString("phoneNo"));
        person.setEmail(rs.getString("email"));
        person.setAddress(rs.getString("address"));
        person.setPhotoUrl(rs.getString("photoUrl"));
        person.setIsWalkIn(rs.getBoolean("isWalkIn"));
        person.setCreatedAt(rs.getTimestamp("createdAt"));
        person.setUpdatedAt(rs.getTimestamp("updatedAt"));
        person.setApprovalStatus(rs.getString("approvalStatus"));
        person.setRejectionReason(rs.getString("rejectionReason"));
        return person;
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private boolean hasColumn(String tableName, String columnName) throws SQLException {
        try (ResultSet rs = getConnection().getMetaData().getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = getConnection().getMetaData().getColumns(null, null, tableName.toUpperCase(), columnName)) {
            return rs.next();
        }
    }

    private boolean isRequiredColumnWithoutDefault(String tableName, String columnName) throws SQLException {
        try (ResultSet rs = getConnection().getMetaData().getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                int nullable = rs.getInt("NULLABLE");
                String defaultValue = rs.getString("COLUMN_DEF");
                return nullable == DatabaseMetaData.columnNoNulls && defaultValue == null;
            }
        }
        return false;
    }
}
