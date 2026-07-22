package examstaff.dao;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.UserDTO;

import java.util.List;

/**
 * Cổng truy cập phân công sát hạch viên theo kỳ thi ({@code ExaminerSchedule}).
 *
 * Vai trò trong kiến trúc:
 *
 * Quản lý slot giám khảo ↔ khu vực ↔ section trước khi auto-allocate thí sinh:
 * chỉ phân thí sinh vào phòng/sân đã có giám khảo được gán. Ghi/xóa slot kèm
 * nhật ký {@code Audit} (triển khai impl).
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
 * - {@link #getActiveExaminers} — user role Examiner đang active
 * - {@link #assign} / {@link #remove} — ghi/xóa slot theo {@code slotKey}
 * - {@link #getByExamId} — toàn bộ ca phân công của kỳ
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ExaminerAssignmentDAOImpl}.
 */
public interface ExaminerAssignmentDAO {

    /**
     * Lấy danh sách sát hạch viên đang hoạt động.
     * Thực thi SELECT trên {@code [User]} INNER JOIN {@code [Role]} (vai trò examiner),
     * LEFT JOIN {@code Profile} lấy họ tên; lọc tài khoản active.
     * @return danh sách {@link UserDTO} sát hạch viên; rỗng nếu không có
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Phân công sát hạch viên vào một slot (kỳ thi / khu vực / section).
     * Kiểm tra trùng rồi INSERT vào {@code ExaminerSchedule}
     * (ExamId, ExaminerId, ExamAreaId, ExamSectionId, AssignedBy, AssignedAt);
     * có thể ghi nhật ký {@code Audit}.
     * @param slot thông tin slot phân công ({@link ExaminerSlotDTO}: exam, examiner, area…)
     * @return {@code true} nếu phân công thành công; {@code false} nếu trùng hoặc lỗi
     */
    boolean assign(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công sát hạch viên theo khóa slot.
     * DELETE trên {@code ExaminerSchedule} (và có thể dọn {@code Audit} liên quan)
     * theo định danh slot ({@code slotKey}).
     * @param slotKey khóa định danh slot cần xóa (thường gồm examId / area / section…)
     * @return {@code true} nếu xóa thành công; {@code false} nếu không tìm thấy hoặc lỗi
     */
    boolean remove(String slotKey);

    /**
     * Lấy toàn bộ slot phân công của một kỳ thi.
     * Thực thi SELECT trên {@code ExaminerSchedule} JOIN {@code Exam}, {@code Licence},
     * {@code [User]}, {@code Profile}, {@code ExamArea}, {@code ExamSection}
     * với {@code WHERE ExamId = ?}.
     * @param examId mã kỳ thi cần liệt kê phân công
     * @return danh sách {@link ExaminerSlotDTO}; rỗng nếu chưa phân công
     */
    List<ExaminerSlotDTO> getByExamId(int examId);
}
