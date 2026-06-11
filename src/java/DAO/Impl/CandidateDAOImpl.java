package DAO.Impl;



import DAO.CandidateDAO;

import DBConnection.DBContext;

import Models.Candidate;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.HashMap;

import java.util.Map;

import java.util.Optional;



public class CandidateDAOImpl extends DBContext implements CandidateDAO {



    private Boolean tableAvailable;



    @Override

    public boolean isTableAvailable() {

        if (tableAvailable != null) {

            return tableAvailable;

        }

        try {

            ensureConnection();

            try (ResultSet rs = connection.getMetaData().getTables(null, null, "Candidate", new String[]{"TABLE"})) {

                tableAvailable = rs.next();

            }

        } catch (SQLException e) {

            tableAvailable = false;

        }

        return tableAvailable;

    }



    @Override

    public Optional<String> findSbdByRegistrationId(int registrationId) {

        String sql = """

                select candidateNo

                from Candidate

                where examRegistrationId = ?

                """;



        return querySingleSbd(sql, registrationId);

    }



    @Override

    public Optional<String> findSbdByPersonAndSession(int personId, int examSessionId) {

        String sql = """

                select candidateNo

                from Candidate

                where personId = ?

                  and examSessionId = ?

                """;



        return querySingleSbd(sql, personId, examSessionId);

    }



    @Override

    public Map<Integer, String> findSbdMapByRegistrationForPerson(int personId) {

        String sql = """

                select examRegistrationId, candidateNo

                from Candidate

                where personId = ?

                  and examRegistrationId is not null

                """;



        return queryRegistrationSbdMap(sql, personId);

    }



    @Override

    public Map<Integer, String> findSbdMapBySessionForPerson(int personId) {

        String sql = """

                select examSessionId, candidateNo

                from Candidate

                where personId = ?

                  and examRegistrationId is null

                """;



        Map<Integer, String> sbdMap = new HashMap<>();



        try {

            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        String sbd = trimToNull(rs.getString("candidateNo"));

                        if (sbd != null) {

                            sbdMap.put(rs.getInt("examSessionId"), sbd);

                        }

                    }

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }



        return sbdMap;

    }



    @Override

    public int insertImported(Candidate candidate) {

        String sql = """

                insert into Candidate (

                    examSessionId, personId, examRegistrationId, candidateNo,

                    govIdNo, fullName, dateOfBirth, licenseCode, importedBy)

                output inserted.id

                values (?, ?, ?, ?, ?, ?, ?, ?, ?)

                """;



        try {

            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, candidate.getExamSessionId());

                if (candidate.getPersonId() != null) {

                    ps.setInt(2, candidate.getPersonId());

                } else {

                    ps.setNull(2, java.sql.Types.INTEGER);

                }

                if (candidate.getExamRegistrationId() != null) {

                    ps.setInt(3, candidate.getExamRegistrationId());

                } else {

                    ps.setNull(3, java.sql.Types.INTEGER);

                }

                ps.setString(4, candidate.getCandidateNo());

                ps.setString(5, candidate.getGovIdNo());

                ps.setString(6, candidate.getFullName());

                ps.setDate(7, candidate.getDateOfBirth());

                ps.setString(8, candidate.getLicenseCode());

                if (candidate.getImportedBy() != null) {

                    ps.setInt(9, candidate.getImportedBy());

                } else {

                    ps.setNull(9, java.sql.Types.INTEGER);

                }



                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        return rs.getInt(1);

                    }

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }



        return -1;

    }



    @Override

    public int linkImportedToRegistrations(int examSessionId) {

        String sql = """

                update c

                set c.personId = p.id,

                    c.examRegistrationId = er.id

                from Candidate c

                inner join Person p on p.govIdNo = c.govIdNo

                inner join ExamRegistration er on er.personId = p.id

                    and er.examSessionId = c.examSessionId

                    and er.isCancelled = 0

                where c.examSessionId = ?

                  and c.personId is null

                """;



        try {

            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, examSessionId);

                return ps.executeUpdate();

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }



        return 0;

    }



    private Map<Integer, String> queryRegistrationSbdMap(String sql, int personId) {

        Map<Integer, String> sbdMap = new HashMap<>();



        try {

            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        String sbd = trimToNull(rs.getString("candidateNo"));

                        if (sbd != null) {

                            sbdMap.put(rs.getInt("examRegistrationId"), sbd);

                        }

                    }

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }



        return sbdMap;

    }



    private Optional<String> querySingleSbd(String sql, int... params) {

        try {

            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {

                    ps.setInt(i + 1, params[i]);

                }

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        return Optional.ofNullable(trimToNull(rs.getString("candidateNo")));

                    }

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }



        return Optional.empty();

    }



    private String trimToNull(String value) {

        if (value == null) {

            return null;

        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;

    }

}

