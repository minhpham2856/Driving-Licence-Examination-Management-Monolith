package examstaff.service;

import examstaff.dto.ExamSelectRequestDTO;
import examstaff.dto.ExamSelectResultDTO;

public interface ExamSelectService {

    ExamSelectResultDTO processSelection(ExamSelectRequestDTO request);
}
