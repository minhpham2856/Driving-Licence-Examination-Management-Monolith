package examstaff.dao;

import examstaff.dto.ExaminerSlotDTO;

import examstaff.dto.UserDTO;

import java.util.List;

/**
 * DAO phân công giám khảo theo kỳ thi ({@code ExaminerSchedule}).
 */
public interface ExaminerAssignmentDAO {

    /**
     * Lấy danh sách giám khảo đang hoạt động.
     *
     * @return danh sách user giám khảo
     */
    List<UserDTO> getActiveExaminers();

    /**
     * Gán giám khảo vào khu vực của kỳ thi.
     *
     * @param slot thông tin slot phân công
     * @return {@code true} nếu ghi thành công
     */
    boolean assign(ExaminerSlotDTO slot);

    /**
     * Gỡ phân công theo khóa slot ({@code examId:areaId:examinerId}).
     *
     * @param slotKey khóa slot dạng ghép
     * @return {@code true} nếu xóa thành công
     */
    boolean remove(String slotKey);

    /**
     * Lấy danh sách slot phân công của một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách slot
     */
    List<ExaminerSlotDTO> getByExamId(int examId);
}
