package examstaff.service;

import shared.model.ExamArea;

import java.util.List;

public interface ExamAreaQueryService {

    /** Phòng LT gắn kỳ và đã có sát hạch viên - dùng dropdown phân phòng thí sinh. */
    List<ExamArea> listStaffedTheoryRoomsForExam(int examId);

    /** Sân/phòng TH gắn kỳ và đã có sát hạch viên. */
    List<ExamArea> listStaffedPracticalAreasForExam(int examId);

    ExamArea findById(int examAreaId);
}
