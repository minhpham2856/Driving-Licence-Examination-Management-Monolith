package examstaff.dao.impl;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai {@link CallBoardDAO} lưu {@link CallBoardState} trong bộ nhớ JVM
 * (singleton, không dùng {@code ServletContext}, không ghi SQL).
 */
public final class InMemoryCallBoardDAO implements CallBoardDAO {

    private static final InMemoryCallBoardDAO INSTANCE = new InMemoryCallBoardDAO();

    private final Map<Integer, CallBoardState> boards = new ConcurrentHashMap<>();
    private volatile Integer activeExamId;

    private InMemoryCallBoardDAO() {
    }

    /** Trả về repository in-memory dùng chung cho toàn ứng dụng. */
    public static InMemoryCallBoardDAO getInstance() {
        return INSTANCE;
    }

    @Override
    public CallBoardState getState(int examId) {
        CallBoardState state = boards.get(examId);
        return state != null ? copy(state) : null;
    }

    @Override
    public void saveState(int examId, CallBoardState state) {
        if (state == null) {
            return;
        }
        boards.put(examId, copy(state));
    }

    @Override
    public void setActiveExamId(int examId) {
        activeExamId = examId > 0 ? examId : null;
    }

    @Override
    public Integer getActiveExamId() {
        Integer id = activeExamId;
        return id != null && id > 0 ? id : null;
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
