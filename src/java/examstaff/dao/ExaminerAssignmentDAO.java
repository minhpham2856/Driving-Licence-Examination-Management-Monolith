package examstaff.dao;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.UserDTO;

import java.util.List;

/**
 * Cổng truy cập phân công sát hạch viên theo kỳ thi (ExaminerSchedule).
 *
 * Vai trò trong kiến trúc:
 *
 * Quản lý slot giám khảo ↔ khu vực ↔ section trước khi auto-allocate thí sinh:
 * chỉ phân thí sinh vào phòng/sân đã có giám khảo được gán. Ghi/xóa slot kèm
 * nhật ký Audit (triển khai impl).
 * <pre>
 *   /examstaff/examiner-allocation
 *            │  assign / remove / list
 *            ▼
 *      ExaminerAssignmentDAO  ◄── ExaminerAssignmentDAOImpl
 *            │
 *            ▼  ExaminerSchedule JOIN ExamArea, ExamSection, User, Profile
 *         DLEM_DB_2
 * </pre>
 *
 * Nhóm API:
 * - getActiveExaminers — user role Examiner đang active
 * - assign / remove — ghi/xóa slot theo slotKey
 * - getByExamId — toàn bộ ca phân công của kỳ
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.ExaminerAssignmentDAOImpl.
 */
public interface ExaminerAssignmentDAO {

    /**
     * Lấy danh sách sát hạch viên đang hoạt động.
     * Thực thi SELECT trên [User] INNER JOIN [Role] (vai trò examiner),
     * LEFT JOIN Profile lấy họ tên; lọc tài khoản active.
     * @return danh sách UserDTO sát hạch viên; rỗng nếu không có
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Phân công sát hạch viên vào một slot (kỳ thi / khu vực / section).
     * Kiểm tra trùng rồi INSERT vào ExaminerSchedule
     * (ExamId, ExaminerId, ExamAreaId, ExamSectionId, AssignedBy, AssignedAt);
     * có thể ghi nhật ký Audit.
     * @param slot thông tin slot phân công (ExaminerSlotDTO: exam, examiner, area…)
     * @return true nếu phân công thành công; false nếu trùng hoặc lỗi
     */
    boolean assign(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công sát hạch viên theo khóa slot.
     * DELETE trên ExaminerSchedule (và có thể dọn Audit liên quan)
     * theo định danh slot (slotKey).
     * @param slotKey khóa định danh slot cần xóa (thường gồm examId / area / section…)
     * @return true nếu xóa thành công; false nếu không tìm thấy hoặc lỗi
     */
    boolean remove(String slotKey);

    /**
     * Lấy toàn bộ slot phân công của một kỳ thi.
     * Thực thi SELECT trên ExaminerSchedule JOIN Exam, Licence,
     * [User], Profile, ExamArea, ExamSection
     * với WHERE ExamId = ?.
     * @param examId mã kỳ thi cần liệt kê phân công
     * @return danh sách ExaminerSlotDTO; rỗng nếu chưa phân công
     */
    List<ExaminerSlotDTO> getByExamId(int examId);
}
