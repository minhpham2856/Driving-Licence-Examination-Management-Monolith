package DAOs;

import DTOs.ExaminerAnswerStats;
import DTOs.ExaminerPaperState;

import java.util.List;
import java.util.Map;

public interface ExaminerSessionDataDAO {
    String findExamCodeBySessionId(int sessionId);

    Map<Integer, ExaminerPaperState> findPaperStatesBySessionId(int sessionId);

    Map<Integer, ExaminerAnswerStats> findAnswerStatsBySessionId(int sessionId);

    List<Map<String, Object>> findDevicesBySessionId(int sessionId);

    List<Map<String, Object>> findScoreDeductions();

    List<Map<String, Object>> findScoreDeductionsBySectionId(int examSectionId);

    Integer findExamSectionIdForSession(int sessionId);

    Map<String, Object> findSessionExportMeta(int sessionId);

    List<Map<String, Object>> findScoreViolationRows(int sessionId);
}
