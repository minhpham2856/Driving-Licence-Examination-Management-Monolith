package examstaff.dao.impl;

import examstaff.dao.CallBoardAttributeKeys;
import examstaff.dao.CallBoardDAO;
import jakarta.servlet.ServletContext;
import examstaff.dto.view.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores CallBoardState on ServletContext — constructed at the controller/HTTP edge. */
public class ServletContextCallBoardDAO implements CallBoardDAO {

    private final ServletContext servletContext;

    public ServletContextCallBoardDAO(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public CallBoardState getState(int examId) {
        CallBoardState state = boards().get(examId);
        return state != null ? copy(state) : null;
    }

    @Override
    public void saveState(int examId, CallBoardState state) {
        if (state == null) {
            return;
        }
        boards().put(examId, copy(state));
    }

    @Override
    public void setActiveExamId(int examId) {
        servletContext.setAttribute(CallBoardAttributeKeys.ACTIVE_EXAM_ID, examId);
    }

    @Override
    public Integer getActiveExamId() {
        Object value = servletContext.getAttribute(CallBoardAttributeKeys.ACTIVE_EXAM_ID);
        if (value instanceof Integer) {
            Integer id = (Integer) value;
            if (id > 0) {
                return id;
            }
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
        copy.setExamId(source.getExamId());
        copy.setCallingSbd(source.getCallingSbd());
        copy.setNextSbd(source.getNextSbd());
        copy.setShiftEnded(source.isShiftEnded());
        copy.setExamPaused(source.isExamPaused());
        copy.setUpdatedAtMs(source.getUpdatedAtMs());
        copy.setQueueOrderSbds(source.getQueueOrderSbds());
        copy.setDeskBusy(source.isDeskBusy());
        copy.setDeskSbd(source.getDeskSbd());
        return copy;
    }
}
