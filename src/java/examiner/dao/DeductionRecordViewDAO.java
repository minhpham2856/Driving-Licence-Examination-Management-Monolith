package examiner.dao;

import java.util.List;
import java.util.Map;

// DAO contract for DeductionRecordView persistence; examiner module SQL boundary.
public interface DeductionRecordViewDAO {

    // Returns joined violation summary rows for all candidates in one exam.
    List<Map<String, Object>> getViolationRowsForExam(int examId);

    // Returns practical deduction rows for one candidate and section.
    List<Map<String, Object>> getDeductionRowsForCandidate(int examId, int sbd, String sectionType);
}
