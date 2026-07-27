package policestaff.dto;

import managingstaff.dto.DossierDTO;

public class PoliceCandidateDTO {
    private int registrationDateId;
    private int examRegistrationId;
    private String policeStatus;
    private String policeReason;
    private String officialCandidateNumber;
    private boolean retake;
    private DossierDTO dossier;

    public int getRegistrationDateId() { return registrationDateId; }
    public void setRegistrationDateId(int value) { registrationDateId = value; }
    public int getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(int value) { examRegistrationId = value; }
    public String getPoliceStatus() { return policeStatus; }
    public void setPoliceStatus(String value) { policeStatus = value; }
    public String getPoliceReason() { return policeReason; }
    public void setPoliceReason(String value) { policeReason = value; }
    public String getOfficialCandidateNumber() { return officialCandidateNumber; }
    public void setOfficialCandidateNumber(String value) { officialCandidateNumber = value; }
    public boolean isRetake() { return retake; }
    public void setRetake(boolean value) { retake = value; }
    public String getRegistrationTypeLabel() {
        return retake ? "Đăng ký thi lại" : "Thi lần đầu";
    }
    public DossierDTO getDossier() { return dossier; }
    public void setDossier(DossierDTO value) { dossier = value; }
    public boolean isPending() { return "PENDING".equalsIgnoreCase(policeStatus); }
    public boolean isApproved() { return "APPROVED".equalsIgnoreCase(policeStatus); }
    public boolean isRejected() { return "REJECTED".equalsIgnoreCase(policeStatus); }
}
