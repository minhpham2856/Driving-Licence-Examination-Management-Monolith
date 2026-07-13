package DAOs;

import Models.Audit;
import DTOs.AuditDTO;
import DTOs.StaffProcedureKpiDTO;
import java.util.List;

/**
 * DAO cho thao tác với nhật ký kiểm tra (AuditLog) trong hệ thống.
 * Cung cấp các phương thức ghi nhật ký, truy vấn nhật ký theo người dùng,
 * ngày tháng, kỳ thi, hỗ trợ phân trang và thống kê KPI cho cán bộ.
 */
public interface AuditLogDAO {

    /**
     * Ghi một bản ghi nhật ký kiểm tra mới.
     *
     * @param log đối tượng Audit chứa thông tin nhật ký
     * @return true nếu ghi thành công
     */
    boolean insert(Audit log);

    /**
     * Lấy danh sách nhật ký của người dùng trong ngày hôm nay.
     *
     * @param userId mã người dùng
     * @return danh sách AuditDTO
     */
    List<AuditDTO> getLogsByUserToday(int userId);

    /**
     * Lấy tất cả nhật ký trong ngày hôm nay.
     *
     * @return danh sách tất cả AuditDTO trong ngày
     */
    List<AuditDTO> getAllLogsToday();

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày cụ thể.
     *
     * @param userId mã người dùng
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return danh sách AuditDTO
     */
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);

    /**
     * Lấy tất cả nhật ký theo ngày cụ thể.
     *
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return danh sách AuditDTO
     */
    List<AuditDTO> getAllLogsByDate(String dateStr);

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày có phân trang.
     *
     * @param userId   mã người dùng
     * @param dateStr  ngày cần lọc (định dạng yyyy-MM-dd)
     * @param page     số trang (bắt đầu từ 1)
     * @param pageSize số lượng bản ghi trên mỗi trang
     * @return danh sách AuditDTO theo trang
     */
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    /**
     * Lấy tất cả nhật ký theo ngày có phân trang.
     *
     * @param dateStr  ngày cần lọc (định dạng yyyy-MM-dd)
     * @param page     số trang (bắt đầu từ 1)
     * @param pageSize số lượng bản ghi trên mỗi trang
     * @return danh sách AuditDTO theo trang
     */
    List<AuditDTO> getAllLogsByDatePaginated(String dateStr, int page, int pageSize);

    /**
     * Đếm số lượng nhật ký của người dùng theo ngày.
     *
     * @param userId  mã người dùng
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return số lượng bản ghi nhật ký
     */
    int getLogsCountByUserAndDate(int userId, String dateStr);

    /**
     * Đếm tổng số nhật ký theo ngày.
     *
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd)
     * @return số lượng bản ghi nhật ký
     */
    int getAllLogsCountByDate(String dateStr);

    /**
     * Tìm nhật ký của một người dùng với bộ lọc và phân trang tại database.
     * Các tham số chuỗi có thể để trống để bỏ qua điều kiện tương ứng.
     */
    List<AuditDTO> searchUserLogsPaginated(int userId, String keyword, String action,
            String startDate, String endDate, int page, int pageSize);

    /** Đếm nhật ký của một người dùng theo cùng bộ lọc tìm kiếm. */
    int countUserLogs(int userId, String keyword, String action,
            String startDate, String endDate);

    /**
     * Lấy chỉ số KPI thủ tục của cán bộ (số thí sinh đã có ảnh + thanh toán do cán bộ đó thu).
     *
     * @param userId     mã cán bộ
     * @param filterDate ngày lọc (định dạng yyyy-MM-dd) hoặc null để lấy tất cả
     * @return StaffProcedureKpiDTO chứa thông tin KPI
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);

    /**
     * Lấy danh sách nhật ký theo kỳ thi có phân trang.
     *
     * @param sessionId mã kỳ thi
     * @param page      số trang (bắt đầu từ 1)
     * @param pageSize  số lượng bản ghi trên mỗi trang
     * @return danh sách AuditDTO theo trang
     */
    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize);

    /**
     * Đếm số lượng nhật ký theo kỳ thi.
     *
     * @param sessionId mã kỳ thi
     * @return số lượng bản ghi nhật ký
     */
    int getLogsCountForSession(int sessionId);

    /**
     * Lấy danh sách nhật ký theo kỳ thi có phân trang và tìm kiếm.
     *
     * @param sessionId   mã kỳ thi
     * @param page        số trang (bắt đầu từ 1)
     * @param pageSize    số lượng bản ghi trên mỗi trang
     * @param searchQuery từ khóa tìm kiếm
     * @return danh sách AuditDTO theo trang
     */
    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    /**
     * Đếm số lượng nhật ký theo kỳ thi có tìm kiếm.
     *
     * @param sessionId   mã kỳ thi
     * @param searchQuery từ khóa tìm kiếm
     * @return số lượng bản ghi nhật ký
     */
    int getLogsCountForSession(int sessionId, String searchQuery);

    /**
     * Lấy danh sách nhật ký vi phạm theo kỳ thi với giới hạn số lượng.
     *
     * @param sessionId mã kỳ thi
     * @param limit     số lượng tối đa bản ghi trả về
     * @return danh sách AuditDTO các vi phạm
     */
    List<AuditDTO> getViolationLogsForSession(int sessionId, int limit);
}
