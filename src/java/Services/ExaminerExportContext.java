package Services;

import Constants.ExamSectionType;
import Controllers.Staff.ExamStaff.ExaminerSlot;

public record ExaminerExportContext(
        int sessionId,
        ExaminerSlot slot,
        ExamSectionType sectionType,
        String sectionName) {
}
