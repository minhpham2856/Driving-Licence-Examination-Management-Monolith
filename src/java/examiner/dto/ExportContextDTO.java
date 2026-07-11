package examiner.dto;

import examiner.model.ExaminerSchedule;

public record ExportContextDTO(int examId, ExaminerSchedule schedule,
        boolean isTheory, String sectionName) {

}
