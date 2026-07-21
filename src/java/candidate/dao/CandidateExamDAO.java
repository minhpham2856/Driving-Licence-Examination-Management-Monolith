package candidate.dao;

import candidate.dto.CandidateExamContext;
import candidate.dto.ExamResultView;
import candidate.dto.QuestionView;
import java.util.List;
import java.util.Map;

/**
 * Data access cho luồng thi lý thuyết của Candidate (schema mới DLEM_DB_2).
 */
public interface CandidateExamDAO {

    /** Tìm ngữ cảnh thi theo số báo danh (Candidate.CandidateNumber). null nếu không có/không được phân phần thi lý thuyết. */
    CandidateExamContext findContextByCandidateNumber(String candidateNumber);

    /** Tạo/khởi động TheoryPaper cho 1 ExamEnrollmentSection; trả về TheoryPaperId. */
    int startTheoryPaper(int examEnrollmentSectionId);

    /** Bốc ngẫu nhiên n câu theo hạng (Licence_Question), đảm bảo có 1 câu điểm liệt nếu bộ đề có. */
    List<QuestionView> loadRandomQuestions(int licenceId, int n);

    /** Lưu bài làm + chấm + ghi ExamResult/ExamScore + đánh dấu section hoàn tất. */
    ExamResultView submitAndGrade(int theoryPaperId, int examEnrollmentId, int examSectionId,
                                  int examEnrollmentSectionId,
                                  List<QuestionView> questions, Map<Integer, String> answers,
                                  int passThreshold);
}
