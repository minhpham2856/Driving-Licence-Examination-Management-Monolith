package examstaff.dao;

import examstaff.dto.CallBoardState;

/**
 * Truy cập trạng thái bảng gọi thí sinh runtime (Call Board).
 * Triển khai mặc định: {@link examstaff.dao.impl.InMemoryCallBoardDAO} (in-memory JVM, không ghi SQL).
 */
public interface CallBoardDAO {

    /**
     * Lấy trạng thái bảng gọi theo kỳ thi.
     * Đọc map boards từ storage runtime in-memory,
     * trả về bản copy an toàn của {@link CallBoardState}.
     *
     * @param examId mã kỳ thi cần lấy trạng thái bảng gọi
     * @return bản copy {@link CallBoardState} nếu đã khởi tạo; {@code null} nếu chưa có
     */
    CallBoardState getState(int examId);

    /**
     * Lưu / ghi đè trạng thái bảng gọi của kỳ thi.
     * Ghi {@link CallBoardState} vào storage runtime theo {@code examId};
     * bỏ qua nếu {@code state} là {@code null}.
     *
     * @param examId mã kỳ thi cần lưu trạng thái
     * @param state  trạng thái mới của bảng gọi; {@code null} thì không cập nhật
     */
    void saveState(int examId, CallBoardState state);

    /**
     * Đặt kỳ thi đang active trên bảng gọi công khai (Public Call).
     * Ghi kỳ thi đang active trên storage runtime.
     *
     * @param examId mã kỳ thi đang chiếu / gọi thí sinh công khai
     */
    void setActiveExamId(int examId);

    /**
     * Đọc kỳ thi đang active cho màn hình Public Call.
     * Đọc kỳ thi đang active từ storage runtime.
     *
     * @return {@code examId} &gt; 0 nếu đã set; {@code null} nếu chưa đặt kỳ active
     */
    Integer getActiveExamId();
}
