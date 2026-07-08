package dao.impl;

import dao.CallBoardAttributeKeys;
import dao.CallBoardDAO;
import jakarta.servlet.ServletContext;
import model.view.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores CallBoardState on ServletContext — constructed at the controller/HTTP edge. */
public class ServletContextCallBoardDAO implements CallBoardDAO {

    private final ServletContext servletContext;

    public ServletContextCallBoardDAO(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public CallBoardState getState(int examSessionId) {
        CallBoardState state = boards().get(examSessionId);
        return state != null ? copy(state) : null;
    }

    @Override
    public void saveState(int examSessionId, CallBoardState state) {
        if (state == null) {
            return;
        }
        boards().put(examSessionId, copy(state));
    }

    @Override
    public void setActiveSessionId(int examSessionId) {
        servletContext.setAttribute(CallBoardAttributeKeys.ACTIVE_SESSION_ID, examSessionId);
    }

    @Override
    public Integer getActiveSessionId() {
        Object value = servletContext.getAttribute(CallBoardAttributeKeys.ACTIVE_SESSION_ID);
        if (value instanceof Integer id && id > 0) {
            return id;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CallBoardState> boards() {
        Map<Integer, CallBoardState> boards = (Map<Integer, CallBoardState>) servletContext
                .getAttribute(CallBoardAttributeKeys.BOARDS_MAP);
        if (boards == null) {
            boards = new ConcurrentHashMap<>();
            servletContext.setAttribute(CallBoardAttributeKeys.BOARDS_MAP, boards);
        }
        return boards;
    }

    private static CallBoardState copy(CallBoardState source) {
        CallBoardState copy = new CallBoardState();
        copy.setExamSessionId(source.getExamSessionId());
        copy.setCallingSbd(source.getCallingSbd());
        copy.setNextSbd(source.getNextSbd());
        copy.setShiftEnded(source.isShiftEnded());
        copy.setUpdatedAtMs(source.getUpdatedAtMs());
        copy.setQueueOrderSbds(source.getQueueOrderSbds());
        copy.setDeskBusy(source.isDeskBusy());
        copy.setDeskSbd(source.getDeskSbd());
        return copy;
    }
}
