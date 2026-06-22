package DAOs;

import DTOs.TheoryPaperAnswerDTO;
import java.util.List;

public interface TheoryPaperDAO {

    List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd);

    int countQuestionsBySessionAndSbd(int sessionId, String sbd);
}
