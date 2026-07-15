package examstaff.dao.impl;

import examstaff.dao.CallBoardAttributeKeys;
import examstaff.dao.CallBoardDAO;
import jakarta.servlet.ServletContext;
import examstaff.dto.CallBoardState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai {@link CallBoardDAO} lưu {@link CallBoardState} trên {@link ServletContext}
 * (bảng tin gọi thí sinh trong bộ nhớ ứng dụng, không truy cập CSDL).
 */
public class ServletContextCallBoardDAO implements CallBoardDAO {

    private final ServletContext servletContext;

    /**
     * Khởi tạo DAO với ngữ cảnh servlet để đọc/ghi thuộc tính ứng dụng.
     *
     * @param servletContext ngữ cảnh servlet của ứng dụng web
     */
    public ServletContextCallBoardDAO(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Lấy trạng thái bảng tin gọi thí sinh của một kỳ thi từ map trên {@link ServletContext}.
     *
     * @param examId mã kỳ thi
     * @return bản sao {@link CallBoardState} nếu đã lưu; {@code null} nếu chưa có
     */
    @Override
    public CallBoardState getState(int examId) {
        // Đọc map trạng thái theo examId từ ServletContext
        CallBoardState state = boards().get(examId);
        // Trả bản sao để tránh sửa trực tiếp object trong map
        return state != null ? copy(state) : null;
    }

    /**
     * Lưu (ghi đè) trạng thái bảng tin gọi thí sinh cho một kỳ thi vào {@link ServletContext}.
     *
     * @param examId mã kỳ thi
     * @param state  trạng thái cần lưu; bỏ qua nếu {@code null}
     */
    @Override
    public void saveState(int examId, CallBoardState state) {
        if (state == null) {
            return;
        }
        // Ghi bản sao vào map để tránh rò rỉ tham chiếu ngoài
        boards().put(examId, copy(state));
    }

    /**
     * Đặt mã kỳ thi đang hoạt động trên bảng tin (thuộc tính toàn cục của ứng dụng).
     *
     * @param examId mã kỳ thi đang active
     */
    @Override
    public void setActiveExamId(int examId) {
        // Ghi thuộc tính ACTIVE_EXAM_ID lên ServletContext
        servletContext.setAttribute(CallBoardAttributeKeys.ACTIVE_EXAM_ID, examId);
    }

    /**
     * Đọc mã kỳ thi đang hoạt động từ thuộc tính {@link ServletContext}.
     *
     * @return mã kỳ thi nếu hợp lệ ({@code > 0}); {@code null} nếu chưa đặt hoặc giá trị không hợp lệ
     */
    @Override
    public Integer getActiveExamId() {
        // Đọc thuộc tính ACTIVE_EXAM_ID từ ServletContext
        Object value = servletContext.getAttribute(CallBoardAttributeKeys.ACTIVE_EXAM_ID);
        if (value instanceof Integer) {
            Integer id = (Integer) value;
            if (id > 0) {
                return id;
            }
        }
        return null;
    }

    /**
     * Lấy (hoặc khởi tạo lần đầu) map {@code examId → CallBoardState} trên {@link ServletContext}.
     *
     * @return map trạng thái bảng tin, luôn khác {@code null}
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, CallBoardState> boards() {
        // Đọc map hiện có từ ServletContext
        Map<Integer, CallBoardState> boards = (Map<Integer, CallBoardState>) servletContext
                .getAttribute(CallBoardAttributeKeys.BOARDS_MAP);
        if (boards == null) {
            // Khởi tạo map thread-safe lần đầu và gắn vào context
            boards = new ConcurrentHashMap<>();
            servletContext.setAttribute(CallBoardAttributeKeys.BOARDS_MAP, boards);
        }
        return boards;
    }

    /**
     * Tạo bản sao sâu (shallow copy các trường) của {@link CallBoardState}.
     *
     * @param source trạng thái nguồn
     * @return bản sao độc lập với cùng dữ liệu
     */
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
