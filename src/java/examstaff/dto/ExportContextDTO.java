package examstaff.dto;

import examstaff.model.ExaminerSchedule;

public record ExportContextDTO(int examId, ExaminerSchedule schedule,
        boolean isTheory, String sectionName) {

}
