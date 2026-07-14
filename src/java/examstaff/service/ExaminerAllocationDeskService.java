package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;

import java.util.List;
import java.util.Map;

/**
 * Nghiệp vụ bàn phân công sát hạch viên: xem danh sách và gán/gỡ slot.
 */
public interface ExaminerAllocationDeskService {

    /**
     * Xây dựng view phân công sát hạch viên cho kỳ thi.
     *
     * @param examId         mã kỳ thi ưu tiên
     * @param fallbackExamId mã kỳ dự phòng
     * @param allExams       danh sách kỳ thi ngữ cảnh
     * @return DTO view phân công
     */
    ExaminerAllocationViewDTO buildAllocationView(int examId, int fallbackExamId, List<ExamSummaryDTO> allExams);

    /**
     * Lập map mã người dùng → thông tin sát hạch viên đang active.
     *
     * @return map sát hạch viên theo userId
     */
    Map<Integer, UserDTO> buildExaminerMap();

    /**
     * Gán sát hạch viên vào khu vực của kỳ thi.
     *
     * @param targetExamId   mã kỳ thi đích
     * @param areaId         mã khu vực/phòng
     * @param examinerUserId mã người dùng sát hạch viên
     * @param staffId        mã nhân viên thực hiện
     * @return kết quả thao tác gán
     */
    ExaminerAllocationActionResultDTO assignExaminer(int targetExamId, int areaId,
            int examinerUserId, int staffId);

    /**
     * Gỡ phân công sát hạch viên khỏi slot.
     *
     * @param slotKey khóa slot phân công
     * @return kết quả thao tác gỡ
     */
    ExaminerAllocationActionResultDTO removeExaminer(String slotKey);
}
