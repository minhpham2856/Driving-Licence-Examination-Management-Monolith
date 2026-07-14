package examstaff.dto;

/**
 * Kết quả tự động phân bổ thí sinh.
 * Trả số lượng đã phân bổ và thông báo lỗi (nếu có) cho Presentation.
 */
public class AutoAllocateResultDTO {
    public int allocatedCount = 0;
    public String errorMsg;
}
