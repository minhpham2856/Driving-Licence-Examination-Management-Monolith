package examstaff.dao.impl;

import examstaff.dao.CallBoardAttributeKeys;
import examstaff.dao.CallBoardDAO;
import jakarta.servlet.ServletContext;
import examstaff.dto.view.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lưu {@link CallBoardState} trên ServletContext (in-memory, theo examId).
 * Chỉ tạo ở biên Presentation/HTTP — không dùng trong BLL thuần.
 */
public class ServletContextCallBoardDAO implements CallBoardDAO {

    private final ServletContext servletContext;

    /**
     * @param servletContext context ứng dụng web chứa map boards + activeExamId
     */
    public ServletContextCallBoardDAO(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /** {@inheritDoc} — trả bản copy để tránh sửa state trong map từ bên ngoài. */
    @Override
    public CallBoardState getState(int examId) {
        CallBoardState state = boards().get(examId);
        return state != null ? copy(state) : null;
    }

    /** {@inheritDoc} — lưu bản copy vào map concurrent. */
    @Override
    public void saveState(int examId, CallBoardState state) {
        if (state == null) {
            return;
        }
        boards().put(examId, copy(state));
    }

    /** {@inheritDoc} */
    @Override
    public void setActiveExamId(int examId) {
        servletContext.setAttribute(CallBoardAttributeKeys.ACTIVE_EXAM_ID, examId);
    }

    /** {@inheritDoc} */
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

    /** Lấy hoặc khởi tạo map boards trên ServletContext. */
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

    /** Sao chép nông các field trạng thái bảng gọi. */
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
