package examstaff.dto;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

public class CandidateDossierViewDTO {

    private ExamRegistrationDTO profile;
    private ExamSummaryDTO examSession;
    private ProcedureFeeResultDTO fees;
    private boolean hasPhotoFile;
    private String dossierTitle;
    private String dossierSubtitle;

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    public ExamSummaryDTO getExamSession() {
        return examSession;
    }

    public void setExamSession(ExamSummaryDTO examSession) {
        this.examSession = examSession;
    }

    public ProcedureFeeResultDTO getFees() {
        return fees;
    }

    public void setFees(ProcedureFeeResultDTO fees) {
        this.fees = fees;
    }

    public boolean isHasPhotoFile() {
        return hasPhotoFile;
    }

    public void setHasPhotoFile(boolean hasPhotoFile) {
        this.hasPhotoFile = hasPhotoFile;
    }

    public String getDossierTitle() {
        return dossierTitle;
    }

    public void setDossierTitle(String dossierTitle) {
        this.dossierTitle = dossierTitle;
    }

    public String getDossierSubtitle() {
        return dossierSubtitle;
    }

    public void setDossierSubtitle(String dossierSubtitle) {
        this.dossierSubtitle = dossierSubtitle;
    }
}
