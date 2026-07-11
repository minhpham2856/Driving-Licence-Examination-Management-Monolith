package service;

import dto.examstaff.ExamSelectRequestDTO;
import dto.examstaff.ExamSelectResultDTO;

public interface ExamSelectService {

    ExamSelectResultDTO processSelection(ExamSelectRequestDTO request);
}
