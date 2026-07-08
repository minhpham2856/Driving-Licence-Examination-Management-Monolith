package dao.view.impl;

import dao.view.ReportInfractionViewDAO;
import dbconnection.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportInfractionViewDAOImpl implements ReportInfractionViewDAO {

    @Override
    public List<Map<String, Object>> findTopInfractions(int limit) {
        List<Map<String, Object>> infractions = new ArrayList<>();
        int top = limit > 0 ? limit : 3;
        String sql = "select top " + top + " sd.[Reason] as deductionReason, count(*) as countVal "
                + "from Score_Deduction sdd "
                + "join ScoreDeduction sd on sd.ScoreDeductionId = sdd.ScoreDeductionId "
                + "group by sd.[Reason] "
                + "order by countVal desc";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infractions;
    }
}
