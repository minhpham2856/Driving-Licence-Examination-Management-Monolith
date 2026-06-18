package Services;

import Controllers.Staff.ExamStaff.ExaminerSlot;
import Utils.ExamConstants.SectionType;

public record ExaminerExportContext(
        int sessionId,
        ExaminerSlot slot,
        SectionType sectionType,
        String sectionName) {
}
