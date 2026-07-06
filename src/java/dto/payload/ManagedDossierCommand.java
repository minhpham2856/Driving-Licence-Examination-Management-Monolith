package dto.payload;

import java.util.Map;

public class ManagedDossierCommand {

    private int profileId;
    private String licenceClass;
    private String applicantType;
    private Map<String, String> documents;
    private int actorUserId;

    public ManagedDossierCommand() {
    }

    public ManagedDossierCommand(int profileId, String licenceClass, String applicantType,
            Map<String, String> documents, int actorUserId) {
        this.profileId = profileId;
        this.licenceClass = licenceClass;
        this.applicantType = applicantType;
        this.documents = documents;
        this.actorUserId = actorUserId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }

    public Map<String, String> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, String> documents) {
        this.documents = documents;
    }

    public int getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(int actorUserId) {
        this.actorUserId = actorUserId;
    }
}
