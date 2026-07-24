package examiner.dao;

import shared.model.ExamEnrollmentSection;
import java.util.List;
import java.util.Map;

// DAO contract for ExamEnrollmentSection persistence; examiner module SQL boundary.
public interface ExamEnrollmentSectionDAO {

    // Returns all section rows for one enrollment.
    List<ExamEnrollmentSection> getAllByEnrollmentId(int examEnrollmentId);

    // Status for each enrollment, scoped to one SectionType.
    Map<Integer, String> getStatusByEnrollmentIds(List<Integer> enrollmentIds, String sectionType);

    // Updates Status for the matching section type row only.
    boolean updateStatusByEnrollmentIdAndSectionType(int examEnrollmentId, String sectionType, String status);

    // Writes assigned room/yard on the section row for this enrollment.
    boolean updateExamAreaIdByEnrollmentIdAndSectionType(int examEnrollmentId, String sectionType, int examAreaId);

    // Reads assigned room/yard for one enrollment + section (0 if none).
    int getIfAreaIdByEnrollmentAndSection(int examEnrollmentId, String sectionType);

    // Ensures a section row exists for enrollment + ExamSection; returns its id.
    int getOrCreate(int examEnrollmentId, int examSectionId);

    // Finds the theory ExamEnrollmentSectionId for an enrollment (0 if none).
    int getIfTheorySectionIdByEnrollment(int examEnrollmentId);

    // Creates the section row for sectionType when missing (Pending status).
    boolean ensureSectionRow(int examEnrollmentId, String sectionType);

    // Batch-loads whether result forms were printed for each enrollment in a section.
    Map<Integer, Boolean> getResultPrintedByEnrollmentIds(List<Integer> enrollmentIds, String sectionType);

    // Returns true when ResultPrintedAt is set for the enrollment section row.
    boolean isResultPrinted(int examEnrollmentId, String sectionType);

    // Stamps ResultPrintedAt on the section row when the result form is printed.
    boolean markResultPrinted(int examEnrollmentId, String sectionType);
}
