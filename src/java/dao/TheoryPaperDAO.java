package dao;


import dto.TheoryPaperAnswerDTO;

import java.util.List;

import model.TheoryPaper;


public interface TheoryPaperDAO {

    
    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);

    
    List<TheoryPaper> getAllByExamEnrollmentIds(List<Integer> examEnrollmentIds);
}
