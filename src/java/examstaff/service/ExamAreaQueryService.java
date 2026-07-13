package examstaff.service;

import shared.model.ExamArea;

import java.util.List;

public interface ExamAreaQueryService {

    List<ExamArea> listActiveTheoryRooms();

    /** PhÃ²ng LT gáº¯n ká»³ vÃ  Ä‘Ã£ cÃ³ giÃ¡m kháº£o â€” dÃ¹ng dropdown phÃ¢n phÃ²ng thÃ­ sinh. */
    List<ExamArea> listStaffedTheoryRoomsForExam(int examId);

    /** SÃ¢n/phÃ²ng TH gáº¯n ká»³ vÃ  Ä‘Ã£ cÃ³ giÃ¡m kháº£o. */
    List<ExamArea> listStaffedPracticalAreasForExam(int examId);

    ExamArea findById(int examAreaId);
}

