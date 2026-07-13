package examstaff.dao;

import examstaff.dto.view.CallBoardState;

/**
 * Data-access for runtime candidate-call board state.
 * Persistence may be in-memory (ServletContext) rather than SQL.
 */
public interface CallBoardDAO {

    CallBoardState getState(int examSessionId);

    void saveState(int examSessionId, CallBoardState state);

    void setActiveExamId(int examSessionId);

    Integer getActiveExamId();
}

