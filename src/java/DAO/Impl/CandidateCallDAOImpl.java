package DAO.Impl;

import DBConnection.DBContext;
import DAO.CandidateCallDAO;
import Models.CandidateCall;
import java.sql.*;

public class CandidateCallDAOImpl extends DBContext implements CandidateCallDAO {

    @Override
    public boolean insert(CandidateCall call) {
        String sql = """
                     insert into CandidateCall (examSessionId, candidateNo, calledTo, calledBy, calledAt, result)
                     values (?, ?, ?, ?, getutcdate(), ?)
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, call.getExamSessionId());
            ps.setInt(2, call.getCandidateNo());
            ps.setString(3, call.getCalledTo());
            ps.setInt(4, call.getCalledBy() != 0 ? call.getCalledBy() : 3); // Defaults to user ID 3
            
            if (call.getResult() == null) {
                ps.setNull(5, Types.NVARCHAR);
            } else {
                ps.setString(5, call.getResult());
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
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
