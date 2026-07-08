package dao;

import model.view.CallBoardState;

/**
 * Data-access for runtime candidate-call board state.
 * Persistence may be in-memory (ServletContext) rather than SQL.
 */
public interface CallBoardDAO {

    CallBoardState getState(int examSessionId);

    void saveState(int examSessionId, CallBoardState state);

    void setActiveSessionId(int examSessionId);

    Integer getActiveSessionId();
}
