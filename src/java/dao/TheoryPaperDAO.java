package dao;


import dto.score.TheoryPaperAnswerDTO;

import java.util.List;

import model.exam.TheoryPaper;

/**
 * DAO cho thao tác với bài thi lý thuyết (TheoryPaper).
 * Cung cấp các phương thức truy vấn câu trả lời và đếm số câu hỏi
 * của thí sinh trong kỳ thi lý thuyết.
 */
public interface TheoryPaperDAO {

    /**
     * Tìm kiếm bài thi lý thuyết theo mã ứng viên trong kỳ thi.
     *
     * @param examCandidateId mã ứng viên trong kỳ thi
     * @return TheoryPaper model hoặc null nếu không có
     */
    TheoryPaper findByExamCandidateId(int examCandidateId);
}
