package examstaff.dao;


import shared.model.Audit;
import examstaff.dto.AuditDTO;
import examstaff.dto.CandidateCallDTO;
import examstaff.dto.StaffProcedureKpiDTO;

import java.util.List;

/**
 * Cổng truy cập nhật ký kiểm tra (Audit) cho module exam staff.
 *
 * Vai trò trong kiến trúc:
 * Ghi lại mọi thao tác quan trọng của cán bộ (INSERT nhật ký, gọi thí sinh, thu lệ phí…)
 * và phục vụ màn tra cứu nhật ký / KPI thủ tục. Không chứa logic nghiệp vụ — chỉ INSERT/SELECT
 * trên bảng Audit (kèm JOIN User/Profile khi list).
 * <pre>
 *   ProcedureServlet / CandidateCallServlet / PaymentServlet
 *            │  insert / insertCall
 *            ▼
 *      AuditLogDAO  ◄── AuditLogDAOImpl (JDBC)
 *            ▲
 *            │  getLogsByUserAndDate* / getStaffProcedureKpi
 *   /examstaff/audit (list + phân trang + KPI)
 * </pre>
 *
 * Nhóm API:
 * - <b>Ghi</b> — insert, insertCall (Action = CALL)
 * - <b>Đọc nhật ký</b> — getLogsByUserAndDate, phân trang, getLogsCountByUserAndDate
 * - <b>KPI thủ tục</b> — getStaffProcedureKpi (Audit + Candidate + Payment)
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.AuditLogDAOImpl — SQL tĩnh AUDIT_SELECT và
 * STAFF_PROCEDURE_KPI_*; KPI join Audit → Candidate → ExamEnrollment → Payment.
 */
public interface AuditLogDAO {

    /**
     * Ghi một bản ghi nhật ký kiểm tra mới.
     * Thực thi INSERT vào bảng Audit với các cột UserId, Action, Reason,
     * EntityName, EntityId, OldValue, NewValue, Details, CreatedAt.
     * @param log đối tượng Audit chứa thông tin nhật ký cần ghi
     * @return true nếu INSERT thành công; false nếu thất bại
     */
    boolean insert(Audit log);

    /**
     * Ghi sự kiện gọi thí sinh vào bảng Audit.
     * Thực thi INSERT vào Audit với Action = CALL, kèm thông tin
     * thí sinh / kỳ thi từ CandidateCallDTO.
     * @param call dữ liệu lượt gọi thí sinh (user, SBD, examId, thời điểm…)
     * @return true nếu ghi nhật ký thành công; false nếu thất bại
     */
    boolean insertCall(CandidateCallDTO call);

    /**
     * Lấy danh sách nhật ký của một người dùng theo ngày cụ thể.
     * Thực thi SELECT trên bảng Audit, LEFT JOIN [User] và Profile
     * để lấy tên người thay đổi; lọc theo UserId và CAST(CreatedAt AS DATE).
     * Có thể giới hạn TOP khi không lọc ngày.
     * @param userId  mã người dùng (UserId) cần lấy nhật ký
     * @param dateStr ngày lọc định dạng yyyy-MM-dd; null/rỗng = không lọc ngày
     * @return danh sách AuditDTO sắp xếp theo CreatedAt giảm dần; rỗng nếu không có
     */
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày có phân trang.
     * Thực thi SELECT tương tự String) kèm
     * OFFSET ? ROWS FETCH NEXT ? ROWS ONLY trên SQL Server.
     * @param userId   mã người dùng (UserId)
     * @param dateStr  ngày lọc định dạng yyyy-MM-dd; null/rỗng = không lọc ngày
     * @param page     số trang bắt đầu từ 1
     * @param pageSize số bản ghi trên mỗi trang
     * @return danh sách AuditDTO của trang tương ứng; rỗng nếu ngoài phạm vi
     */
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    /**
     * Đếm số lượng nhật ký của người dùng theo ngày.
     * Thực thi SELECT COUNT(*) FROM Audit với điều kiện UserId
     * và tùy chọn CAST(CreatedAt AS DATE).
     * @param userId  mã người dùng (UserId)
     * @param dateStr ngày lọc định dạng yyyy-MM-dd; null/rỗng = đếm tất cả theo user
     * @return số lượng bản ghi nhật ký thỏa điều kiện (≥ 0)
     */
    int getLogsCountByUserAndDate(int userId, String dateStr);

    /**
     * Lấy chỉ số KPI thủ tục của cán bộ: số thí sinh đã có ảnh và thanh toán
     * gắn với thao tác của cán bộ đó trên Audit.
     * Thực thi SELECT tổng hợp trên Audit JOIN Candidate,
     * ExamEnrollment, Payment; lọc theo hành động INSERT/UPDATE
     * và tùy chọn ngày.
     * @param userId     mã cán bộ (UserId)
     * @param filterDate ngày lọc định dạng yyyy-MM-dd, hoặc null để lấy tất cả
     * @return StaffProcedureKpiDTO chứa số liệu KPI; không null (có thể count = 0)
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);
}
