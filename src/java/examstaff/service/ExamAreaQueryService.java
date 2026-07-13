package examstaff.service;

import shared.model.ExamArea;

import java.util.List;

public interface ExamAreaQueryService {

    /** Phòng LT gắn kỳ và đã có giám khảo — dùng dropdown phân phòng thí sinh. */
    List<ExamArea> listStaffedTheoryRoomsForExam(int examId);

    /** Sân/phòng TH gắn kỳ và đã có giám khảo. */
    List<ExamArea> listStaffedPracticalAreasForExam(int examId);

    ExamArea findById(int examAreaId);
}
