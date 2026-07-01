package dto;
public record ExaminerExportContext(int sessionId, ExaminerSlotDTO slot,
        boolean isTheory, String sectionName) {
}
