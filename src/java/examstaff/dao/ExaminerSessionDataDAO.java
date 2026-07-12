package examstaff.dao;


import examstaff.dto.examiner.ExaminerAnswerStatsDTO;

import examstaff.dto.examiner.ExaminerPaperStateDTO;


import java.util.List;
import java.util.Map;

 // DAO interface for fetching examiner-facing session data.
public interface ExaminerSessionDataDAO {

         // Returns the exam code associated with the given session.
    String findExamCodeBySessionId(int sessionId);

         // Returns a map of candidate ID to paper state (started / submitted)
    Map<Integer, ExaminerPaperStateDTO> findPaperStatesBySessionId(int sessionId);

         // Returns a map of candidate ID to answer statistics (correct / wrong / unanswered)
    Map<Integer, ExaminerAnswerStatsDTO> findAnswerStatsBySessionId(int sessionId);

         // Returns a list of devices (computers / equipment) linked to the
    List<Map<String, Object>> findDevicesBySessionId(int sessionId);

         // Lists all score deductions (trừ điểm) available globally.
    List<Map<String, Object>> findScoreDeductions();

         // Lists score deductions that are scoped to a specific exam section.
    List<Map<String, Object>> findScoreDeductionsBySectionId(int examSectionId);

         // Returns the first exam section ID associated with a session.
    Integer findExamSectionIdForSession(int sessionId);

         // Fetches export metadata for the given session (name, exam code, date, times).
    Map<String, Object> findSessionExportMeta(int sessionId);

         // Returns violation rows (score deductions) for all candidates in a session.
    List<Map<String, Object>> findScoreViolationRows(int sessionId);
}
