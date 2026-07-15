package examstaff.dao;

/**
 * Khóa attribute trên {@code ServletContext} dùng bởi triển khai {@link CallBoardDAO}
 * (ví dụ lưu map bảng gọi và kỳ thi đang active in-memory, không qua SQL).
 * <p>
 * Lớp hằng — không thể khởi tạo.
 */
public final class CallBoardAttributeKeys {

    /**
     * Attribute chứa {@code Map} trạng thái bảng gọi theo {@code examId}
     * (giá trị kiểu map của {@link examstaff.dto.CallBoardState}).
     */
    public static final String BOARDS_MAP = "candidateCallBoards";

    /**
     * Attribute chứa mã kỳ thi đang active trên màn hình Public Call
     * ({@link CallBoardDAO#getActiveExamId()} / {@link CallBoardDAO#setActiveExamId(int)}).
     */
    public static final String ACTIVE_EXAM_ID = "activeCallExamId";

    /**
     * Chặn khởi tạo: chỉ dùng các hằng khóa attribute.
     */
    private CallBoardAttributeKeys() {
    }
}
