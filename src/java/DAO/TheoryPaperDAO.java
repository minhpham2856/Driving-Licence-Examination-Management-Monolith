package DAO;

import Models.TheoryPaperAnswer;
import java.util.List;

public interface TheoryPaperDAO {
    List<TheoryPaperAnswer> getAnswersBySessionAndSbd(int sessionId, String sbd);

    int countQuestionsBySessionAndSbd(int sessionId, String sbd);
}
