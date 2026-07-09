package dto;

import model.ExaminerSchedule;

public record ExportContextDTO(int sessionId, ExaminerSchedule schedule,
        boolean isTheory, String sectionName) {

}
