package examstaff.dto.examiner;

import shared.enums.SectionType;

public record ExaminerExportContext(int sessionId, ExaminerSlotDTO slot,
        SectionType sectionType, String sectionName) {

}

