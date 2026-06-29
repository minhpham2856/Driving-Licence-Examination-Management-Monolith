package service;

import dto.ServiceResult;
import dto.TheoryEntranceDTO;
import dto.TheorySubmitDTO;
import model.Question;

import java.util.List;
import java.util.Map;

public interface TheoryService {

    Integer getActiveExamId();

    ServiceResult<TheoryEntranceDTO> validateEntrance(int sbd);

    double scanFace(int examId, int sbd);

    List<Question> loadExamQuestions(int examId, int sbd);

    void saveDraftAnswers(int examId, int sbd, Map<Integer, String> answers);

    ServiceResult<TheorySubmitDTO> submitExam(int examId, int sbd, Map<Integer, String> answers);
}
