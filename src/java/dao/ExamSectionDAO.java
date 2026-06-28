package dao;


import model.exam.ExamSection;
import java.util.List;


public interface ExamSectionDAO {

    
    ExamSection findById(int examSectionId);

    
    List<ExamSection> findAll();

    
    List<ExamSection> findBySessionId(int sessionId);
}
