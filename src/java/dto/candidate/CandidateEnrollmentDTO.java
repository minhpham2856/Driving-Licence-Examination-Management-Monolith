package dto.candidate;

import model.exam.ExamEnrollment;

public class CandidateEnrollmentDTO {

    private CandidateProfileDTO candidate;
    private ExamEnrollment enrollment;

    public CandidateEnrollmentDTO() {
    }

    public CandidateEnrollmentDTO(CandidateProfileDTO candidate, ExamEnrollment enrollment) {
        this.candidate = candidate;
        this.enrollment = enrollment;
    }

    public CandidateProfileDTO getCandidate() {
        return candidate;
    }

    public void setCandidate(CandidateProfileDTO candidate) {
        this.candidate = candidate;
    }

    public ExamEnrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(ExamEnrollment enrollment) {
        this.enrollment = enrollment;
    }

    // Convenience getters for old JSP
    public int getId() {
        return candidate != null ? candidate.getCandidateId() : 0;
    }

    public String getSbd() {
        return candidate != null ? candidate.getCandidateNumber() : "";
    }

    public String getCandidateNo() {
        return candidate != null ? candidate.getCandidateNumber() : "";
    }

    public String getFullName() {
        return candidate != null ? candidate.getFullName() : "";
    }

    public String getGovIdNo() {
        return candidate != null ? candidate.getGovernmentIdNumber() : "";
    }

    public boolean isAbsent() {
        return candidate != null && candidate.isAbsent();
    }

    public boolean isSuspended() {
        return candidate != null && candidate.isSuspended();
    }

    public String getSectionStatus() {
        return enrollment != null ? enrollment.getSectionStatus() : "Pending";
    }

    public void setSectionStatus(String status) {
        if (enrollment != null) {
            enrollment.setSectionStatus(status);
        }
    }

    public boolean isSignaturePrinted() {
        return enrollment != null && enrollment.isSignaturePrinted();
    }
}
