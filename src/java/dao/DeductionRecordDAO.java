package dao;
import java.util.List;
import java.util.Map;
public interface DeductionRecordDAO {
    List<Map<String, Object>> getViolationRowsForSession(int sessionId);
    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);
}
