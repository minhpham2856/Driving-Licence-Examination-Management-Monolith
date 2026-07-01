package examiner.dao.impl;

import examiner.dao.DeductionRecordViewDAO;
import dbconnection.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeductionRecordViewDAOImpl extends DBContext implements DeductionRecordViewDAO {

    @Override
    public List<Map<String, Object>> getViolationRowsForExam(int examId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT c.CandidateNumber AS sbd, "
                + "       c.FullName AS fullName, "
                + "       sec.SectionName AS sectionName, "
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
                    row.put("sectionName", rs.getString("sectionName"));
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
}
