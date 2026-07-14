package examstaff.dao;

import examstaff.dto.view.CallBoardState;

/**
 * Truy cập trạng thái bảng gọi thí sinh runtime.
 * Có thể lưu in-memory (ServletContext) thay vì SQL.
 */
public interface CallBoardDAO {

    /**
     * Lấy trạng thái bảng gọi theo kỳ thi (bản copy an toàn).
     *
     * @param examId mã kỳ thi
     * @return trạng thái hoặc null nếu chưa khởi tạo
     */
    CallBoardState getState(int examId);

    /**
     * Lưu / ghi đè trạng thái bảng gọi của kỳ thi.
     *
     * @param examId mã kỳ thi
     * @param state  trạng thái mới (bỏ qua nếu null)
     */
    void saveState(int examId, CallBoardState state);

    /**
     * Đặt kỳ thi đang active trên bảng gọi công khai.
     *
     * @param examId mã kỳ thi
     */
    void setActiveExamId(int examId);

    /**
     * Đọc kỳ thi đang active cho Public Call.
     *
     * @return examId &gt; 0, hoặc null nếu chưa set
     */
    Integer getActiveExamId();
}
