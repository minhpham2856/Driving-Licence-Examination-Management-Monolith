package examiner.service;

import shared.enums.CandidateStatus;
import shared.enums.SectionType;

// Service contract for per-section enrollment progress and result-print tracking.
public interface ProgressService {

    // Reads normalized candidate status for one enrollment section row.
    CandidateStatus get(int examEnrollmentId, SectionType sectionType);

    // Updates candidate status on the enrollment section row.
    boolean update(int examEnrollmentId, SectionType sectionType, CandidateStatus status);

    // Ensures an ExamEnrollmentSection row exists for the enrollment and section.
    boolean add(int examEnrollmentId, SectionType sectionType);

    // Returns whether the result form was marked printed for this section.
    boolean isResultPrinted(int examEnrollmentId, SectionType sectionType);

    // Sets the result-printed flag for this enrollment section.
    boolean markResultPrinted(int examEnrollmentId, SectionType sectionType);

    // Returns whether practical entry is allowed given theory/layout flags and theory completion.
    boolean isPracticalEntryAllowed(int examEnrollmentId, boolean takeTheory, boolean takeLayout);

    // Returns whether practical attendance is allowed (theory checked-in when takeTheory).
    boolean isPracticalAttendanceAllowed(int examEnrollmentId, boolean takeTheory, boolean takeLayout);
}
