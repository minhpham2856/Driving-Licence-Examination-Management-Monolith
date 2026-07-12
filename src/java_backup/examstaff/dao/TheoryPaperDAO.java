package examstaff.dao;

import shared.model.TheoryPaper;
import examstaff.dto.score.TheoryPaperAnswerDTO;
import java.util.List;

public interface TheoryPaperDAO {

    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);

    // --- CleanMyBranch methods ---
    List<TheoryPaper> getAllByExamEnrollmentIds(List<Integer> examEnrollmentIds);

    List<TheoryPaperAnswerDTO> getAnswersBySessionAndSbd(int sessionId, String sbd);

    int countQuestionsBySessionAndSbd(int sessionId, String sbd);
}

