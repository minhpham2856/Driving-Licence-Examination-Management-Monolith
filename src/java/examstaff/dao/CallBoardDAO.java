package examstaff.dao;

import examstaff.dto.view.CallBoardState;

/**
 * Data-access for runtime candidate-call board state.
 * Persistence may be in-memory (ServletContext) rather than SQL.
 */
public interface CallBoardDAO {

    CallBoardState getState(int examId);

    void saveState(int examId, CallBoardState state);

    void setActiveExamId(int examId);

    Integer getActiveExamId();
}
