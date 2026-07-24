package examstaff.service;

/**
 * Ghi nhật ký thao tác nghiệp vụ của nhân viên kỳ thi.
 */
public interface StaffAuditLogService {

    /**
     * Ghi một dòng audit cho thao tác của người dùng.
     *
     * @param userId   mã người dùng thực hiện
     * @param action   mã/loại hành động
     * @param details  mô tả chi tiết
     * @param recordId mã bản ghi liên quan (0 nếu không có)
     */
    void logAction(int userId, String action, String details, int recordId);
}
