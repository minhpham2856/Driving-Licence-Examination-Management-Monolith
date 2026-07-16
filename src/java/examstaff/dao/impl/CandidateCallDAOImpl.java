package examstaff.dao.impl;


import shared.dbconnection.DBContext;

import examstaff.dao.CandidateCallDAO;

import examstaff.dto.candidate.CandidateCallDTO;
import examstaff.util.CallAuditFormatter;

import java.sql.*;

/**
 * JDBC implementation of CandidateCallDAO for logging candidate call-out events
 * into the Audit table with action = 'CALL'.
 */
public class CandidateCallDAOImpl extends DBContext implements CandidateCallDAO {

    /**
     * Inserts a call-out event as an Audit record with action 'CALL'.
     * The entity ID is composed as "{examId}-{candidateNo}".
     *
     * @param call the call event data (calledBy, examId, candidateNo, calledTo, result)
     * @return true if insertion succeeded
     */
    @Override
    public boolean insert(CandidateCallDTO call) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'CALL', ?, 'Candidate', ?, ?, GETDATE())
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int userId = call.getCalledBy() != 0 ? call.getCalledBy() : 3;
            String entityId = call.getExamId() + "-" + call.getCandidateNo();
            String detail = CallAuditFormatter.formatDetail(call.getCalledTo(), call.getResult());
            ps.setInt(1, userId);
            ps.setString(2, detail);
            ps.setString(3, entityId);
            ps.setString(4, detail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
