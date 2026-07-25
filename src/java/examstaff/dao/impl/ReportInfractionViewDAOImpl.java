package examstaff.dao.impl;

import examstaff.dao.Db2ExamSchemaSql;
import examstaff.dao.ReportInfractionViewDAO;
import shared.dbconnection.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Triển khai JDBC của ReportInfractionViewDAO — thống kê lỗi trừ điểm thực hành.
 *
 * Luồng query:
 * DeductionRecord → ScoreDeduction (lý do) → ExamScore → ExamSection
 * → chỉ section <b>thực hành</b> (Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES) → enrollment theo examId.
 * Gom SUM(OccurrenceCount) theo lý do, TOP (?) cho biểu đồ report.
 */
public class ReportInfractionViewDAOImpl implements ReportInfractionViewDAO {

    /**
     * SQL gom top lý do trừ điểm thực hành theo OccurrenceCount.
     * Lọc SectionType bằng Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES.
     */
    private static final String TOP_INFRACTIONS_SQL = """
            SELECT TOP (?)
                   sd.[Reason] AS deductionReason,
                   SUM(dr.OccurrenceCount) AS countVal
            FROM DeductionRecord dr
            INNER JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId
            INNER JOIN ExamScore es ON es.ExamScoreId = dr.ExamScoreId
            INNER JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
            INNER JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
            INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = er.ExamEnrollmentId
            WHERE ee.ExamId = ?
              AND dr.OccurrenceCount > 0
              AND sec.SectionType IN (""" + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
              )
            GROUP BY sd.ScoreDeductionId, sd.[Reason]
            ORDER BY countVal DESC
            """;

    /**
     * Lấy top lỗi trừ điểm của phần thực hành trong kỳ thi.
     * Truy vấn DeductionRecord JOIN ScoreDeduction, ExamScore,
     * ExamSection, ExamResult, ExamEnrollment theo examId.
     * @param examId mã kỳ thi
     * @param limit  số dòng tối đa (mặc định 3 nếu &le; 0)
     * @return danh sách map gồm reason, count, percentage
     */
    @Override
    public List<Map<String, Object>> findTopInfractions(int examId, int limit) {
        if (examId <= 0) {
            return List.of();
        }
        int top = limit > 0 ? limit : 3;
        List<Map<String, Object>> infractions = new ArrayList<>();
        // Chuẩn bị PreparedStatement với SQL SELECT top lỗi trừ điểm
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(TOP_INFRACTIONS_SQL)) {
            // Gán tham số truy vấn: TOP limit và examId
            ps.setInt(1, top);
            ps.setInt(2, examId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                int totalInfractions = 0;
                while (rs.next()) {
                    // Ánh xạ ResultSet → map reason/count
                    Map<String, Object> map = new HashMap<>();
                    map.put("reason", rs.getString("deductionReason"));
                    int cnt = rs.getInt("countVal");
                    map.put("count", cnt);
                    totalInfractions += cnt;
                    infractions.add(map);
                }
                // Tính phần trăm cho từng lý do sau khi có tổng
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
