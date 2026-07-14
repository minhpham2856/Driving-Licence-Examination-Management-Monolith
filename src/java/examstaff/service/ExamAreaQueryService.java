package examstaff.service;

import shared.model.ExamArea;

import java.util.List;

/**
 * Truy vấn khu vực/phòng thi đã gắn giám khảo phục vụ phân phòng.
 */
public interface ExamAreaQueryService {

    /** Phòng LT gắn kỳ và đã có sát hạch viên - dùng dropdown phân phòng thí sinh. */
    List<ExamArea> listStaffedTheoryRoomsForExam(int examId);

    /** Sân/phòng TH gắn kỳ và đã có sát hạch viên. */
    List<ExamArea> listStaffedPracticalAreasForExam(int examId);

    /**
     * Tìm khu vực thi theo mã.
     *
     * @param examAreaId mã khu vực
     * @return khu vực, hoặc null nếu không có
     */
    ExamArea findById(int examAreaId);
}
