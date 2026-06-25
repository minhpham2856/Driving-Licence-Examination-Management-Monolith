package DAOs;

import DTOs.TheoryPaperAnswerDTO;
import java.util.List;

/**
 * DAO cho thao tác với bài thi lý thuyết (TheoryPaper).
 * Cung cấp các phương thức truy vấn câu trả lời và đếm số câu hỏi
 * của thí sinh trong kỳ thi lý thuyết.
 */
public interface TheoryPaperDAO {

    /**
     * Lấy danh sách câu trả lời của thí sinh theo kỳ thi và số báo danh.
     *
     * @param sessionId mã kỳ thi
     * @param sbd       số báo danh
     * @return danh sách TheoryPaperAnswerDTO chứa câu trả lời
     */
    List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd);

    /**
     * Đếm số lượng câu hỏi của thí sinh trong bài thi lý thuyết.
     *
     * @param sessionId mã kỳ thi
     * @param sbd       số báo danh
     * @return số lượng câu hỏi
     */
    int countQuestionsBySessionAndSbd(int sessionId, String sbd);
}
