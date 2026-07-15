package examiner.dto;

import shared.enums.SectionType;
import shared.model.ExaminerSchedule;

// Export/print session context passed from examiner controllers to document services.
public record ExportContextDTO(int examId, ExaminerSchedule schedule,
        boolean isTheory, SectionType section) {
}
