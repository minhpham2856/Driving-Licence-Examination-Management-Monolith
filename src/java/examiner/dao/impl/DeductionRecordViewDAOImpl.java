package examiner.dao.impl;

import examiner.dao.DeductionRecordViewDAO;
import shared.dbconnection.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// JDBC implementation for DeductionRecordView; examiner module DAO layer only.
public class DeductionRecordViewDAOImpl extends DBContext implements DeductionRecordViewDAO {

    // Returns joined violation summary rows for all candidates in one exam.
    @Override
    public List<Map<String, Object>> getViolationRowsForExam(int examId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT c.CandidateNumber AS sbd, "
                + "       c.FullName AS fullName, "
                + "       sec.SectionType AS sectionType, "
                + "       sd.Reason AS violationReason, "
                + "       sd.Points AS deductionPoints, "
                + "       sd.IsCritical AS critical, "
                + "       es.Score AS currentScore "
                + "FROM ExamEnrollment ec "
                + "JOIN Candidate c ON c.CandidateId = ec.CandidateId "
                + "JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId "
                + "JOIN ExamScore es ON es.ExamResultId = er.ExamResultId "
                + "JOIN DeductionRecord sded ON sded.ExamScoreId = es.ExamScoreId "
                + "JOIN ScoreDeduction sd ON sd.ScoreDeductionId = sded.ScoreDeductionId "
                + "JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId "
                + "WHERE ec.ExamId = ? "
                + "ORDER BY c.CandidateNumber, sd.ScoreDeductionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sbd", rs.getString("sbd"));
                    row.put("fullName", rs.getString("fullName"));
                    row.put("sectionType", rs.getString("sectionType"));
                    row.put("violationReason", rs.getString("violationReason"));
                    row.put("deductionPoints", rs.getDouble("deductionPoints"));
                    row.put("critical", rs.getBoolean("critical"));
                    row.put("currentScore", rs.getDouble("currentScore"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> getDeductionRowsForCandidate(int examId, int sbd, String sectionType) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (examId <= 0 || sbd <= 0 || sectionType == null || sectionType.isBlank()) {
            return rows;
        }
        String sql = "SELECT sd.Reason AS reason, "
                + "       sd.Points AS points, "
                + "       dr.OccurrenceCount AS occurrenceCount, "
                + "       (sd.Points * dr.OccurrenceCount) AS totalDeducted "
                + "FROM ExamEnrollment ee "
                + "JOIN Candidate c ON c.CandidateId = ee.CandidateId "
                + "JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId "
                + "JOIN ExamScore es ON es.ExamResultId = er.ExamResultId "
                + "JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId "
                + "JOIN DeductionRecord dr ON dr.ExamScoreId = es.ExamScoreId "
                + "JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId "
                + "WHERE ee.ExamId = ? "
                + "  AND TRY_CAST(c.CandidateNumber AS INT) = ? "
                + "  AND sec.SectionType = ? "
                + "  AND dr.OccurrenceCount > 0 "
                + "ORDER BY sd.ScoreDeductionId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, sbd);
            ps.setString(3, sectionType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("reason", rs.getString("reason"));
                    row.put("points", rs.getDouble("points"));
                    row.put("occurrenceCount", rs.getInt("occurrenceCount"));
                    row.put("totalDeducted", rs.getDouble("totalDeducted"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
