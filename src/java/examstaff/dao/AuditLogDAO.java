package examstaff.dao;


import shared.model.Audit;
import examstaff.dto.AuditDTO;
import examstaff.dto.CandidateCallDTO;
import examstaff.dto.StaffProcedureKpiDTO;

import java.util.List;

/**
 * DAO cho thao tác với nhật ký kiểm tra ({@code Audit}) trong hệ thống exam staff.
 * Cung cấp các phương thức ghi nhật ký (INSERT), truy vấn nhật ký theo người dùng
 * và ngày (SELECT JOIN {@code Audit}/{@code User}/{@code Profile}), hỗ trợ phân trang
 * OFFSET/FETCH và thống kê KPI thủ tục từ Audit + Payment.
 */
public interface AuditLogDAO {

    /**
     * Ghi một bản ghi nhật ký kiểm tra mới.
     * Thực thi INSERT vào bảng {@code Audit} với các cột UserId, Action, Reason,
     * EntityName, EntityId, OldValue, NewValue, Details, CreatedAt.
     *
     * @param log đối tượng {@link Audit} chứa thông tin nhật ký cần ghi
     * @return {@code true} nếu INSERT thành công; {@code false} nếu thất bại
     */
    boolean insert(Audit log);

    /**
     * Ghi sự kiện gọi thí sinh vào bảng Audit.
     * Thực thi INSERT vào {@code Audit} với Action = {@code CALL}, kèm thông tin
     * thí sinh / kỳ thi từ {@link CandidateCallDTO}.
     *
     * @param call dữ liệu lượt gọi thí sinh (user, SBD, examId, thời điểm…)
     * @return {@code true} nếu ghi nhật ký thành công; {@code false} nếu thất bại
     */
    boolean insertCall(CandidateCallDTO call);

    /**
     * Lấy danh sách nhật ký của một người dùng theo ngày cụ thể.
     * Thực thi SELECT trên bảng {@code Audit}, LEFT JOIN {@code [User]} và {@code Profile}
     * để lấy tên người thay đổi; lọc theo {@code UserId} và {@code CAST(CreatedAt AS DATE)}.
     * Có thể giới hạn TOP khi không lọc ngày.
     *
     * @param userId  mã người dùng ({@code UserId}) cần lấy nhật ký
     * @param dateStr ngày lọc định dạng {@code yyyy-MM-dd}; {@code null}/rỗng = không lọc ngày
     * @return danh sách {@link AuditDTO} sắp xếp theo {@code CreatedAt} giảm dần; rỗng nếu không có
     */
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày có phân trang.
     * Thực thi SELECT tương tự {@link #getLogsByUserAndDate(int, String)} kèm
     * {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY} trên SQL Server.
     *
     * @param userId   mã người dùng ({@code UserId})
     * @param dateStr  ngày lọc định dạng {@code yyyy-MM-dd}; {@code null}/rỗng = không lọc ngày
     * @param page     số trang bắt đầu từ 1
     * @param pageSize số bản ghi trên mỗi trang
     * @return danh sách {@link AuditDTO} của trang tương ứng; rỗng nếu ngoài phạm vi
     */
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    /**
     * Đếm số lượng nhật ký của người dùng theo ngày.
     * Thực thi {@code SELECT COUNT(*) FROM Audit} với điều kiện {@code UserId}
     * và tùy chọn {@code CAST(CreatedAt AS DATE)}.
     *
     * @param userId  mã người dùng ({@code UserId})
     * @param dateStr ngày lọc định dạng {@code yyyy-MM-dd}; {@code null}/rỗng = đếm tất cả theo user
     * @return số lượng bản ghi nhật ký thỏa điều kiện (≥ 0)
     */
    int getLogsCountByUserAndDate(int userId, String dateStr);

    /**
     * Lấy chỉ số KPI thủ tục của cán bộ: số thí sinh đã có ảnh và thanh toán
     * gắn với thao tác của cán bộ đó trên Audit.
     * Thực thi SELECT tổng hợp trên {@code Audit} JOIN {@code Candidate},
     * {@code ExamEnrollment}, {@code Payment}; lọc theo hành động INSERT/UPDATE
     * và tùy chọn ngày.
     *
     * @param userId     mã cán bộ ({@code UserId})
     * @param filterDate ngày lọc định dạng {@code yyyy-MM-dd}, hoặc {@code null} để lấy tất cả
     * @return {@link StaffProcedureKpiDTO} chứa số liệu KPI; không {@code null} (có thể count = 0)
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);
}
