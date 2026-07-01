package examiner.dao.impl;



import examiner.dao.ExamResultDAO;

import dbconnection.DBContext;

import examiner.model.ExamResult;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.sql.Timestamp;



public class ExamResultDAOImpl extends DBContext implements ExamResultDAO {



    @Override

    public int getExamResultIdByExamEnrollmentId(int examEnrollmentId) {

        String sql = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setInt(1, examEnrollmentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt("ExamResultId");

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return 0;

    }



    @Override

    public int add(ExamResult result) {

        String sql = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate) VALUES (?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, result.getExamEnrollmentId());

            ps.setBoolean(2, result.isPassed());

            if (result.getResultDate() != null) {

                ps.setTimestamp(3, result.getResultDate());

            } else {

                ps.setNull(3, java.sql.Types.TIMESTAMP);

            }

            if (ps.executeUpdate() > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {

                    if (keys.next()) {

                        return keys.getInt(1);

                    }

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return 0;

    }



    @Override

    public boolean updatePassed(int examResultId, boolean passed) {

        String sql = "UPDATE ExamResult SET IsPassed = ?, ResultDate = ? WHERE ExamResultId = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setBoolean(1, passed);

            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            ps.setInt(3, examResultId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return false;

    }

}

