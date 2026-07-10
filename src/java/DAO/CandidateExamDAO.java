package DAO;

import Models.CandidateExamContext;
import Models.ExamResultView;
import Models.Question;
import java.util.List;
import java.util.Map;

/**
 * Data access cho luồng thi lý thuyết của Candidate (thí sinh đến thi theo chỉ thị).
 */
public interface CandidateExamDAO {

    /** * Tìm ngữ cảnh thi theo số báo danh (Candidate.CandidateNumber). 
     */
    CandidateExamContext findContextByCandidateNumber(String candidateNumber);

    /** * Máy thi đã phân sẵn cho thí sinh (TheoryPaper.ExamDeviceId), fallback máy khả dụng trong khu vực ca thi. 
     */
    int findAssignedDevice(int examCandidateId, int sessionId);

    /** * Tạo/khởi động TheoryPaper, trả về TheoryPaperId. 
     */
    int startTheoryPaper(int examCandidateId, int deviceId);

    /** * Bốc ngẫu nhiên n câu theo hạng (Licence_Question), đảm bảo có 1 câu điểm liệt. 
     */
    List<Question> loadRandomQuestions(int licenceId, int n);

    /** * Lưu bài làm + chấm điểm + ghi ExamResult/ExamScore, trả về kết quả hiển thị. 
     */
    ExamResultView submitAndGrade(int theoryPaperId, int examCandidateId,
                                  List<Question> questions, Map<Integer, String> answers,
                                  int passThreshold);
}