package examstaff.dto.examiner;

import examstaff.enums.SectionType;

public record ExaminerExportContext(int sessionId, ExaminerSlotDTO slot,
        SectionType sectionType, String sectionName) {

}
