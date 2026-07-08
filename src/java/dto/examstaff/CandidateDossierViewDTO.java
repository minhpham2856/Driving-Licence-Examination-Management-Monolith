package dto.examstaff;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;

public class CandidateDossierViewDTO {

    private ExamRegistrationDTO profile;
    private SessionDTO examSession;
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

    public SessionDTO getExamSession() {
        return examSession;
    }

    public void setExamSession(SessionDTO examSession) {
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
