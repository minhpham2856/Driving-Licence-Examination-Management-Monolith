package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamRegistrationDAO;
import Models.ExamRegistration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    @Override
    public ExamRegistration getById(int id) {
        String sql = """
                     select er.*, 
                            p.fullName, p.govIdNo, p.dateOfBirth, p.gender, p.phoneNo, p.email, p.photoUrl,
                            lt.licenseCode,
                            ec.computerCode,
                            ec.areaId as allocatedAreaId,
                            ea.areaName as allocatedAreaName,
                            ts.finalScore as theoryScore,
                            ps.finalScore as practicalScore,
                            rs.finalScore as roadTestScore
                     from ExamRegistration er
                     join Person p on er.personId = p.id
                     join ExamSession es on er.examSessionId = es.id
                     join LicenseType lt on es.licenseTypeId = lt.id
                     left join ExamPaper ep on ep.id = (select top 1 id from ExamPaper where examRegistrationId = er.id order by id desc)
                     left join ExamComputer ec on ep.examComputerId = ec.id
                     left join ExamArea ea on ec.areaId = ea.id
                     left join TheoryScore ts on ep.id = ts.examPaperId
                     left join PracticalScore ps on er.id = ps.examRegistrationId and ps.examSectionId = 4
                     left join PracticalScore rs on er.id = rs.examRegistrationId and rs.examSectionId = 5
                     where er.id = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExamRegistration getBySessionAndSbd(int sessionId, String sbd) {
        if (sbd == null || !sbd.contains("-")) {
            return null;
        }
        
        try {
            String[] parts = sbd.split("-");
            int candidateNo = Integer.parseInt(parts[1]);

            String sql = """
                         select er.*, 
                                p.fullName, p.govIdNo, p.dateOfBirth, p.gender, p.phoneNo, p.email, p.photoUrl,
                                lt.licenseCode,
                                ec.computerCode,
                                ec.areaId as allocatedAreaId,
                                ea.areaName as allocatedAreaName,
                                ts.finalScore as theoryScore,
                                ps.finalScore as practicalScore,
                                rs.finalScore as roadTestScore
                         from ExamRegistration er
                         join Person p on er.personId = p.id
                         join ExamSession es on er.examSessionId = es.id
                         join LicenseType lt on es.licenseTypeId = lt.id
                         left join ExamPaper ep on ep.id = (select top 1 id from ExamPaper where examRegistrationId = er.id order by id desc)
                         left join ExamComputer ec on ep.examComputerId = ec.id
                         left join ExamArea ea on ec.areaId = ea.id
                         left join TheoryScore ts on ep.id = ts.examPaperId
                         left join PracticalScore ps on er.id = ps.examRegistrationId and ps.examSectionId = 4
                         left join PracticalScore rs on er.id = rs.examRegistrationId and rs.examSectionId = 5
                         where er.examSessionId = ? and er.candidateNo = ?
                         """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                ps.setInt(2, candidateNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToExamRegistration(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExamRegistration> getCandidatesBySession(int sessionId) {
        List<ExamRegistration> list = new ArrayList<>();
        String sql = """
                     select er.*, 
                            p.fullName, p.govIdNo, p.dateOfBirth, p.gender, p.phoneNo, p.email, p.photoUrl,
                            lt.licenseCode,
                            ec.computerCode,
                            ec.areaId as allocatedAreaId,
                            ea.areaName as allocatedAreaName,
                            ts.finalScore as theoryScore,
                            ps.finalScore as practicalScore,
                            rs.finalScore as roadTestScore
                     from ExamRegistration er
                     join Person p on er.personId = p.id
                     join ExamSession es on er.examSessionId = es.id
                     join LicenseType lt on es.licenseTypeId = lt.id
                     left join ExamPaper ep on ep.id = (select top 1 id from ExamPaper where examRegistrationId = er.id order by id desc)
                     left join ExamComputer ec on ep.examComputerId = ec.id
                     left join ExamArea ea on ec.areaId = ea.id
                     left join TheoryScore ts on ep.id = ts.examPaperId
                     left join PracticalScore ps on er.id = ps.examRegistrationId and ps.examSectionId = 4
                     left join PracticalScore rs on er.id = rs.examRegistrationId and rs.examSectionId = 5
                     where er.examSessionId = ?
                     order by er.candidateNo
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExamRegistration(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        String sql = """
                     update ExamRegistration 
                     set isPresent = ?, presentMarkedAt = ? 
                     where id = ?
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, isPresent);
            if (isPresent) {
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        String sql = """
                     update ExamRegistration 
                     set isPaymentCompleted = ? 
                     where id = ?
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, isPaymentCompleted);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateComputer(int id, String computerCode) {
        // We first need to check if an ExamPaper exists for this registration
        // If not, we create one. Then we set the computer ID by looking up computerCode
        try {
            int computerId = -1;
            if (computerCode != null && !computerCode.isEmpty()) {
                String compSql = "select id from ExamComputer where computerCode = ?";
                try (PreparedStatement ps = connection.prepareStatement(compSql)) {
                    ps.setString(1, computerCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            computerId = rs.getInt("id");
                        }
                    }
                }
            }

            // Check if ExamPaper exists
            int paperId = -1;
            String checkPaperSql = "select id from ExamPaper where examRegistrationId = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkPaperSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paperId = rs.getInt("id");
                    }
                }
            }

            if (paperId == -1) {
                // Insert new ExamPaper
                String insSql = """
                                insert into ExamPaper (examRegistrationId, examComputerId, startedAt, isSubmitted)
                                values (?, ?, getutcdate(), 0)
                                """;
                try (PreparedStatement ps = connection.prepareStatement(insSql)) {
                    ps.setInt(1, id);
                    if (computerId != -1) {
                        ps.setInt(2, computerId);
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.executeUpdate();
                }
            } else {
                // Update existing ExamPaper
                String updSql = """
                                update ExamPaper 
                                set examComputerId = ? 
                                where id = ?
                                """;
                try (PreparedStatement ps = connection.prepareStatement(updSql)) {
                    if (computerId != -1) {
                        ps.setInt(1, computerId);
                    } else {
                        ps.setNull(1, Types.INTEGER);
                    }
                    ps.setInt(2, paperId);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDevice(int id, String deviceCode) {
        // deviceCode is stored in notes column as "Device: Bằng lái xe..." or we write it to notes
        String notesVal = (deviceCode != null && !deviceCode.isEmpty()) ? "Device: " + deviceCode : null;
        String sql = """
                     update ExamRegistration 
                     set notes = ? 
                     where id = ?
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (notesVal != null) {
                ps.setString(1, notesVal);
            } else {
                ps.setNull(1, Types.NVARCHAR);
            }
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed) {
        // This handles simulating/inserting scores into TheoryScore and PracticalScore tables.
        try {
            // 1. If theoryScore is provided, update or insert in TheoryScore
            if (theoryScore != null) {
                // Find ExamPaper ID
                int paperId = -1;
                String checkPaperSql = "select id from ExamPaper where examRegistrationId = ?";
                try (PreparedStatement ps = connection.prepareStatement(checkPaperSql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            paperId = rs.getInt("id");
                        }
                    }
                }

                if (paperId == -1) {
                    // Create ExamPaper first
                    String insPaper = "insert into ExamPaper (examRegistrationId, startedAt, isSubmitted) values (?, getutcdate(), 1)";
                    try (PreparedStatement ps = connection.prepareStatement(insPaper, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) {
                                paperId = gk.getInt(1);
                            }
                        }
                    }
                }

                // Check if TheoryScore exists
                boolean scoreExists = false;
                String checkTheory = "select 1 from TheoryScore where examPaperId = ?";
                try (PreparedStatement ps = connection.prepareStatement(checkTheory)) {
                    ps.setInt(1, paperId);
                    try (ResultSet rs = ps.executeQuery()) {
                        scoreExists = rs.next();
                    }
                }

                if (scoreExists) {
                    String upd = "update TheoryScore set finalScore = ? where examPaperId = ?";
                    try (PreparedStatement ps = connection.prepareStatement(upd)) {
                        ps.setInt(1, theoryScore);
                        ps.setInt(2, paperId);
                        ps.executeUpdate();
                    }
                } else {
                    String ins = "insert into TheoryScore (examPaperId, totalRawScore, finalScore) values (?, ?, ?)";
                    try (PreparedStatement ps = connection.prepareStatement(ins)) {
                        ps.setInt(1, paperId);
                        ps.setInt(2, theoryScore / 4); // Simulated raw
                        ps.setInt(3, theoryScore);
                        ps.executeUpdate();
                    }
                }
            }

            // 2. If practicalScore is provided, update or insert in PracticalScore
            if (practicalScore != null) {
                boolean exists = false;
                String checkPrac = "select 1 from PracticalScore where examRegistrationId = ?";
                try (PreparedStatement ps = connection.prepareStatement(checkPrac)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        exists = rs.next();
                    }
                }

                if (exists) {
                    String upd = "update PracticalScore set finalScore = ? where examRegistrationId = ?";
                    try (PreparedStatement ps = connection.prepareStatement(upd)) {
                        ps.setInt(1, practicalScore);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                } else {
                    String ins = """
                                 insert into PracticalScore (examRegistrationId, examSectionId, baseScore, totalDeductions, finalScore, evaluatedBy)
                                 values (?, 4, 100, ?, ?, 2)
                                 """; // Evaluated by Examiner 2
                    try (PreparedStatement ps = connection.prepareStatement(ins)) {
                        ps.setInt(1, id);
                        ps.setInt(2, 100 - practicalScore);
                        ps.setInt(3, practicalScore);
                        ps.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo) {
        try {
            // Find personId
            int personId = -1;
            String sqlId = "select personId from ExamRegistration where id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlId)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        personId = rs.getInt("personId");
                    }
                }
            }

            if (personId != -1) {
                String sqlUpd = """
                                update Person 
                                set fullName = ?, dateOfBirth = ?, govIdNo = ?, email = ?, phoneNo = ?, updatedAt = getutcdate()
                                where id = ?
                                """;
                try (PreparedStatement ps = connection.prepareStatement(sqlUpd)) {
                    ps.setString(1, fullName);
                    ps.setDate(2, dob);
                    ps.setString(3, govIdNo);
                    ps.setString(4, email);
                    ps.setString(5, phoneNo);
                    ps.setInt(6, personId);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        try {
            int personId = -1;
            String sqlId = "select personId from ExamRegistration where id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlId)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        personId = rs.getInt("personId");
                    }
                }
            }

            if (personId != -1) {
                String sqlUpd = "update Person set photoUrl = ?, updatedAt = getutcdate() where id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sqlUpd)) {
                    if (photoUrl != null) {
                        ps.setString(1, photoUrl);
                    } else {
                        ps.setNull(1, Types.NVARCHAR);
                    }
                    ps.setInt(2, personId);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamRegistration mapResultSetToExamRegistration(ResultSet rs) throws SQLException {
        ExamRegistration er = new ExamRegistration();
        er.setId(rs.getInt("id"));
        er.setExamSessionId(rs.getInt("examSessionId"));
        er.setPersonId(rs.getInt("personId"));
        er.setCandidateNo(rs.getInt("candidateNo"));
        er.setRegistrationType(rs.getString("registrationType"));
        er.setIsPaymentCompleted(rs.getBoolean("isPaymentCompleted"));
        er.setIsPresent(rs.getBoolean("isPresent"));
        er.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        
        // Joined candidate personal information
        er.setFullName(rs.getString("fullName"));
        er.setGovIdNo(rs.getString("govIdNo"));
        er.setDateOfBirth(rs.getDate("dateOfBirth"));
        er.setGender(rs.getBoolean("gender"));
        er.setPhoneNo(rs.getString("phoneNo"));
        er.setEmail(rs.getString("email"));
        er.setPhotoUrl(rs.getString("photoUrl"));
        er.setLicenseCode(rs.getString("licenseCode"));

        // Helper allocation status mapping
        er.setComputerCode(rs.getString("computerCode"));
        int areaIdVal = rs.getInt("allocatedAreaId");
        if (rs.wasNull()) {
            er.setAllocatedAreaId(null);
        } else {
            er.setAllocatedAreaId(areaIdVal);
        }
        er.setAllocatedAreaName(rs.getString("allocatedAreaName"));
        
        String notes = rs.getString("notes");
        er.setNotes(notes);
        if (notes != null && notes.startsWith("Device: ")) {
            er.setDeviceCode(notes.replace("Device: ", ""));
        } else {
            er.setDeviceCode("");
        }

        // Scores Mapping
        int tScoreVal = rs.getInt("theoryScore");
        if (rs.wasNull()) {
            er.setTheoryScore(null);
            er.setTheoryPassed("none");
        } else {
            er.setTheoryScore(tScoreVal);
            er.setTheoryPassed(tScoreVal >= 80 ? "passed" : "failed");
        }

        int pScoreVal = rs.getInt("practicalScore");
        if (rs.wasNull()) {
            er.setPracticalScore(null);
            er.setPracticalPassed("none");
        } else {
            er.setPracticalScore(pScoreVal);
            er.setPracticalPassed(pScoreVal >= 80 ? "passed" : "failed");
        }

        int rScoreVal = rs.getInt("roadTestScore");
        if (rs.wasNull()) {
            er.setRoadTestScore(null);
            er.setRoadTestPassed("none");
        } else {
            er.setRoadTestScore(rScoreVal);
            er.setRoadTestPassed(rScoreVal >= 80 ? "passed" : "failed");
        }

        return er;
    }

    @Override
    public boolean insert(ExamRegistration reg) {
        String sql = """
                     insert into ExamRegistration (examSessionId, personId, candidateNo, registrationType, isPaymentCompleted, isPresent, notes)
                     values (?, ?, ?, ?, ?, ?, ?)
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reg.getExamSessionId());
            ps.setInt(2, reg.getPersonId());
            ps.setInt(3, reg.getCandidateNo());
            ps.setString(4, reg.getRegistrationType() != null ? reg.getRegistrationType() : "PreRegistered");
            ps.setBoolean(5, reg.isPaymentCompleted());
            ps.setBoolean(6, reg.isPresent());
            ps.setString(7, reg.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        reg.setId(gk.getInt(1));
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
    public boolean updateRoadScore(int id, Integer roadScore, String roadPassed) {
        try {
            if (roadScore != null) {
                boolean exists = false;
                String checkRoad = "select 1 from PracticalScore where examRegistrationId = ? and examSectionId = 5";
                try (PreparedStatement ps = connection.prepareStatement(checkRoad)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        exists = rs.next();
                    }
                }

                if (exists) {
                    String upd = "update PracticalScore set finalScore = ? where examRegistrationId = ? and examSectionId = 5";
                    try (PreparedStatement ps = connection.prepareStatement(upd)) {
                        ps.setInt(1, roadScore);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                } else {
                    String ins = """
                                 insert into PracticalScore (examRegistrationId, examSectionId, baseScore, totalDeductions, finalScore, evaluatedBy)
                                 values (?, 5, 100, ?, ?, 2)
                                 """;
                    try (PreparedStatement ps = connection.prepareStatement(ins)) {
                        ps.setInt(1, id);
                        ps.setInt(2, 100 - roadScore);
                        ps.setInt(3, roadScore);
                        ps.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExamRegistration> getAllCandidates() {
        List<ExamRegistration> list = new ArrayList<>();
        String sql = """
                     select er.*, 
                            p.fullName, p.govIdNo, p.dateOfBirth, p.gender, p.phoneNo, p.email, p.photoUrl,
                            lt.licenseCode,
                            ec.computerCode,
                            ec.areaId as allocatedAreaId,
                            ea.areaName as allocatedAreaName,
                            ts.finalScore as theoryScore,
                            ps.finalScore as practicalScore,
                            rs.finalScore as roadTestScore
                     from ExamRegistration er
                     join Person p on er.personId = p.id
                     join ExamSession es on er.examSessionId = es.id
                     join LicenseType lt on es.licenseTypeId = lt.id
                     left join ExamPaper ep on ep.id = (select top 1 id from ExamPaper where examRegistrationId = er.id order by id desc)
                     left join ExamComputer ec on ep.examComputerId = ec.id
                     left join ExamArea ea on ec.areaId = ea.id
                     left join TheoryScore ts on ep.id = ts.examPaperId
                     left join PracticalScore ps on er.id = ps.examRegistrationId and ps.examSectionId = 4
                     left join PracticalScore rs on er.id = rs.examRegistrationId and rs.examSectionId = 5
                     order by es.examDate desc, er.candidateNo
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
