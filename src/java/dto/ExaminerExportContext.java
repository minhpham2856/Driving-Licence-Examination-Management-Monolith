package dto;

import enums.SectionType;

public record ExaminerExportContext(int sessionId, ExaminerSlotDTO slot,
        SectionType sectionType, String sectionName) {

}
