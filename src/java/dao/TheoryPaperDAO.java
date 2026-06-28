package dao;


import dto.score.TheoryPaperAnswerDTO;

import java.util.List;

import model.exam.TheoryPaper;


public interface TheoryPaperDAO {

    
    TheoryPaper findByExamEnrollmentId(int examEnrollmentId);

    
    List<TheoryPaper> findByExamEnrollmentIds(List<Integer> examEnrollmentIds);
}
