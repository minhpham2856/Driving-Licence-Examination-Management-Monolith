package dto;

import model.ExaminerSchedule;

public record ExaminerExportContext(int sessionId, ExaminerSchedule schedule,
        boolean isTheory, String sectionName) {

}
