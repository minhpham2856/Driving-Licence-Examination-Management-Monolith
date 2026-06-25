package Services;

import Controllers.Staff.ExamStaff.ExaminerSlot;
import Utils.ExamConstants.SectionType;

 // Immutable record that captures the contextual parameters needed by
public record ExaminerExportContext(
        // The database session ID identifying the active exam session
        int sessionId,
        // The examiner's slot descriptor containing area, exam type, and examiner details
        ExaminerSlot slot,
        // Enum classifying the section as THEORY or SCORE_BASED (practical/road)
        SectionType sectionType,
        // Vietnamese display name of the section (e.g. "Ly thuyet", "Thuc hanh")
        String sectionName) {
}
