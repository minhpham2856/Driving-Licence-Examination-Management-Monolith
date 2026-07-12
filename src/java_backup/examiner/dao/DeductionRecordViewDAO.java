package examiner.dao;

import java.util.List;
import java.util.Map;

public interface DeductionRecordViewDAO {

    List<Map<String, Object>> getViolationRowsForExam(int examId);
}
