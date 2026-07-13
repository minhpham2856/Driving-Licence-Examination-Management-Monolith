package examstaff.dto;

import shared.model.ExaminerSchedule;

public record ExportContextDTO(int examId, ExaminerSchedule schedule,
        boolean isTheory, String sectionName) {

}

