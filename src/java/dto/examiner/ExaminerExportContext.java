package dto.examiner;

import enums.SectionType;
import controller.staff.exam.ExaminerSlot;

public record ExaminerExportContext(
        int sessionId,
        ExaminerSlot slot,
        SectionType sectionType,
        String sectionName) {
}
