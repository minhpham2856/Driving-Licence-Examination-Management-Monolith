package examiner.service;

import examiner.dto.EnrollmentDTO;
import examiner.dto.ServiceResult;
import shared.enums.SectionType;
import shared.model.Payment;
import shared.model.Profile;
import shared.model.User;
import java.util.List;
import java.util.Map;

// Service contract for enrollment lookups, candidate updates, and profile/user persistence helpers.
public interface EnrollmentService {

    // Loads enrollment by exam and candidate number (SBD) with default section context.
    EnrollmentDTO getByExamAndSbd(int examId, int sbd);

    // Loads enrollment by exam, SBD, and section type for section-specific status.
    EnrollmentDTO getByExamAndSbd(int examId, int sbd, SectionType sectionType);

    // Lists all enrollments for an exam with default section context.
    List<EnrollmentDTO> getAllByExam(int examId);

    // Lists all enrollments for an exam with section-specific status and flags.
    List<EnrollmentDTO> getAllByExam(int examId, SectionType sectionType);

    // Searches enrollments by keyword for the exam session (default section).
    List<EnrollmentDTO> getFilteredByExam(int examId, String keyword);

    // Searches enrollments by keyword with section-specific status enrichment.
    List<EnrollmentDTO> getFilteredByExam(int examId, String keyword, SectionType sectionType);

    // Updates a candidate portrait photo URL.
    ServiceResult<Void> updatePhoto(int candidateId, String photoUrl);

    // Marks a candidate as absent for the exam enrollment.
    ServiceResult<Void> markAbsent(int candidateId);

    // Clears the absent marking on a candidate enrollment.
    ServiceResult<Void> clearAbsentMarking(int candidateId);

    // Sets the candidate suspended flag in the database.
    ServiceResult<Void> markSuspended(int candidateId);

    // Clears the candidate suspended flag in the database.
    ServiceResult<Void> undoSuspension(int candidateId);

    // Returns applied score deduction rows for a candidate (stub for examiner views).
    List<Map<String, Object>> getAllAppliedDeductionsByCandidate(int candidateId, int examId);

    // Updates theory and/or practical scores via ScoreService.
    ServiceResult<Void> updateScores(int candidateId, Integer theoryScore, String theoryResult,
            Integer practicalScore, String practicalResult);

    // Updates present/absent state on the candidate record.
    ServiceResult<Void> updatePresent(int candidateId, boolean isPresent);

    // Assigns a candidate to an exam area and picks an active device in that area.
    ServiceResult<Void> updateAllocatedRoom(int candidateId, int areaId, String areaName);

    // Records a completed cash payment for the candidate's latest enrollment.
    ServiceResult<Void> updatePayment(int candidateId, boolean isPaid);

    // Inserts a payment row directly through the DAO.
    boolean add(Payment payment);

    // Loads enrollment DTO by candidate id with theory section status defaults.
    EnrollmentDTO get(int candidateId);

    // Resolves internal candidate id from government id and exam id.
    Integer getIfByGovIdAndExam(String governmentIdNumber, int examId);

    // Inserts a new profile row.
    boolean add(Profile profile);

    // Updates an existing profile row.
    boolean updateProfile(Profile profile);

    // Loads a profile by government identification number.
    Profile getByGovId(String govId);

    // Inserts a new user account row.
    boolean add(User user);

    // Loads a user account by username.
    User getByUsername(String username);
}
