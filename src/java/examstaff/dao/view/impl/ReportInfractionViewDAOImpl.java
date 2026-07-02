package examstaff.dao.view.impl;

import examstaff.dao.view.ReportInfractionViewDAO;
import dbconnection.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportInfractionViewDAOImpl implements ReportInfractionViewDAO {

    private static final String TOP_INFRACTIONS_SQL = """
            SELECT TOP (?) sd.[Reason] AS deductionReason,
                   SUM(dr.OccurrenceCount) AS countVal
            FROM DeductionRecord dr
            INNER JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId
            INNER JOIN ExamScore es ON es.ExamScoreId = dr.ExamScoreId
            INNER JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
            INNER JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
            INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
            WHERE ee.ExamId = ?
              AND dr.OccurrenceCount > 0
              AND sec.SectionType IN (N'Practical', N'Thực hành', N'Sa hình', N'Layout', N'TH')
            GROUP BY sd.ScoreDeductionId, sd.[Reason]
            ORDER BY countVal DESC
            """;

    @Override
    public List<Map<String, Object>> findTopInfractions(int examId, int limit) {
        if (examId <= 0) {
            return List.of();
        }
        int top = limit > 0 ? limit : 3;
        List<Map<String, Object>> infractions = new ArrayList<>();
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(TOP_INFRACTIONS_SQL)) {
            ps.setInt(1, top);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                int totalInfractions = 0;
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reason", rs.getString("deductionReason"));
                    int cnt = rs.getInt("countVal");
                    map.put("count", cnt);
                    totalInfractions += cnt;
                    infractions.add(map);
                }
                for (Map<String, Object> map : infractions) {
                    int cnt = (int) map.get("count");
                    double pct = totalInfractions > 0 ? ((double) cnt / totalInfractions) * 100.0 : 0.0;
                    map.put("percentage", pct);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infractions;
    }
}
