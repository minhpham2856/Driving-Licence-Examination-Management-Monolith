package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.CandidateCallDAO;
import DTOs.CandidateCallDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CandidateCallDAOImpl implements CandidateCallDAO {

    private final DBContext ctx;

    public CandidateCallDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public boolean insert(CandidateCallDTO call) {
        String sql = """
                insert into Audit (UserId, Action, Reason, EntityName, EntityId, NewValue, CreatedAt)
                values (?, 'CALL', ?, 'Candidate', ?, ?, GETDATE())
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
