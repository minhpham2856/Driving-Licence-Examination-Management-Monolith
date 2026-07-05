package service;

import dto.ServiceResult;
import dto.payload.TheoryEntranceData;
import dto.payload.TheorySubmitData;
import jakarta.servlet.ServletContext;
import model.Question;

import java.util.List;
import java.util.Map;

public interface TheoryExamService {

    Integer getActiveSessionId(ServletContext ctx);

    ServiceResult<TheoryEntranceData> validateEntrance(ServletContext ctx, int sbd);

    double scanFace(ServletContext ctx, int sessionId, int sbd);

    List<Question> loadExamQuestions(int sessionId, int sbd);

    void saveDraftAnswers(ServletContext ctx, int sessionId, int sbd, Map<Integer, String> answers);

    ServiceResult<TheorySubmitData> submitExam(ServletContext ctx, int sessionId, int sbd,
            Map<Integer, String> answers);
}
