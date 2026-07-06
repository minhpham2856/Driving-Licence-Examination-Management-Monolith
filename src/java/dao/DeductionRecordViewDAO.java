package dao;

import java.util.List;
import java.util.Map;

public interface DeductionRecordViewDAO {

    List<Map<String, Object>> getViolationRowsForSession(int sessionId);
}
