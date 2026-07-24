package examstaff.dto;

import shared.enums.ErrorType;

/**
 * Bao kết quả nghiệp vụ generic <T> từ BLL ExamStaff về Presentation:
 * thành công / thất bại, loại lỗi, thông điệp và payload tùy chọn.
 *
 * Vai trò trong luồng examstaff:
 * Chuẩn hóa trả về cho các thao tác control ca, thủ tục, phân bổ, chọn kỳ, phân công SHV…
 * Servlet đọc isSuccess(), lấy getMessage() đưa flash/error, lấy getData()
 * khi cần tiếp tục side-effect (ví dụ ExamTransitionResultDTO, AllocationActionResultDTO).
 *
 * Ai tạo:
 * Factory ok/fail trong ExamControlServiceImpl, ProcedureServiceImpl /
 * ProcedureWorkflowServiceImpl, AllocationServiceImpl, ExaminerAssignServiceImpl,
 * ExamStaffSelectionServiceImpl, …
 *
 * Ai tiêu thụ:
 * ExamControlServlet, ExamStaffShiftSupport, ProcedureServlet,
 * AllocationServlet, ExaminerAllocationServlet, ExamSelectServlet.
 *
 * Trang / JSP:
 * Không bind type wrapper; message thường thành errorMsg / alertMsg trên request/session.
 * @param <T> kiểu payload khi thành công (hoặc failure kèm data)
 */
public final class ServiceResult<T> {

    private final boolean success;
    private final ErrorType errorType;
    private final String message;
    private final T data;

    /**
     * Khởi tạo nội bộ — dùng các factory ok/fail.
     * @param success   kết quả nghiệp vụ
     * @param errorType loại lỗi (null khi thành công)
     * @param message   thông điệp hiển thị / log
     * @param data      payload tùy chọn
     */
    private ServiceResult(boolean success, ErrorType errorType, String message, T data) {
        this.success = success;
        this.errorType = errorType;
        this.message = message;
        this.data = data;
    }

    /**
     * Kết quả thành công kèm payload, không message.
     * @param data dữ liệu trả về
     * @param <T>  kiểu payload
     * @return instance success
     */
    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(true, null, null, data);
    }

    /**
     * Kết quả thành công kèm payload và thông điệp (alert thành công).
     * @param data    dữ liệu trả về
     * @param message thông báo UI
     * @param <T>     kiểu payload
     * @return instance success
     */
    public static <T> ServiceResult<T> ok(T data, String message) {
        return new ServiceResult<>(true, null, message, data);
    }

    /**
     * Kết quả thành công chỉ có thông điệp, không payload.
     * @param message thông báo UI
     * @param <T>     kiểu payload (null)
     * @return instance success
     */
    public static <T> ServiceResult<T> okMessage(String message) {
        return new ServiceResult<>(true, null, message, null);
    }

    /**
     * Kết quả thất bại với loại lỗi và thông điệp, không data.
     * @param type    ErrorType phân loại xử lý UI
     * @param message mô tả lỗi
     * @param <T>     kiểu payload
     * @return instance fail
     */
    public static <T> ServiceResult<T> fail(ErrorType type, String message) {
        return new ServiceResult<>(false, type, message, null);
    }

    /**
     * Kết quả thất bại nhưng vẫn kèm payload (ví dụ partial state / outcome).
     * @param type    loại lỗi
     * @param message mô tả lỗi
     * @param data    payload đi kèm failure
     * @param <T>     kiểu payload
     * @return instance fail
     */
    public static <T> ServiceResult<T> fail(ErrorType type, String message, T data) {
        return new ServiceResult<>(false, type, message, data);
    }

    /** true nếu thao tác nghiệp vụ thành công. */
    public boolean isSuccess() {
        return success;
    }

    /** Loại lỗi chuẩn hóa khi thất bại; null khi thành công. */
    public ErrorType getErrorType() {
        return errorType;
    }

    /** Thông điệp thành công hoặc lỗi để hiển thị / flash. */
    public String getMessage() {
        return message;
    }

    /** Payload kết quả (có thể null). */
    public T getData() {
        return data;
    }
}
