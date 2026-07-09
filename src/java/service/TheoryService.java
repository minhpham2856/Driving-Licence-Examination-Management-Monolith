package service;

import dto.ServiceResult;
import dto.TheoryEntranceDTO;
import dto.TheorySubmitDTO;
import model.Question;

import java.util.List;
import java.util.Map;

public interface TheoryService {

    Integer getActiveSessionId();

    ServiceResult<TheoryEntranceDTO> validateEntrance(int sbd);

    double scanFace(int sessionId, int sbd);

    List<Question> loadExamQuestions(int sessionId, int sbd);

    void saveDraftAnswers(int sessionId, int sbd, Map<Integer, String> answers);

    ServiceResult<TheorySubmitDTO> submitExam(int sessionId, int sbd, Map<Integer, String> answers);
}
