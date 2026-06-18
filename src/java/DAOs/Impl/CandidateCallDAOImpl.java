package DAO.Impl;

import DBConnection.DBContext;
import DAO.CandidateCallDAO;
import Models.CandidateCall;
import java.sql.*;

public class CandidateCallDAOImpl extends DBContext implements CandidateCallDAO {

    @Override
    public boolean insert(CandidateCall call) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'CALL', ?, 'Candidate', ?, ?, GETDATE())
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int userId = call.getCalledBy() != 0 ? call.getCalledBy() : 3;
            String entityId = call.getExamSessionId() + "-" + call.getCandidateNo();
            String detail = "calledTo=" + call.getCalledTo()
                    + ";result=" + (call.getResult() != null ? call.getResult() : "");
            ps.setInt(1, userId);
            ps.setString(2, detail);
            ps.setString(3, entityId);
            ps.setString(4, detail);
            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        call.setId(gk.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
