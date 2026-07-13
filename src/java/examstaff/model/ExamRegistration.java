package examstaff.model;

public class ExamRegistration {

    private int examRegistrationId;
    private String registrationStatus;
    private String notes;
    private int profileId;
    private int licenceId;
    private Profile profile;
    private Licence licence;

    public ExamRegistration() {
    }

    public ExamRegistration(int examRegistrationId, String registrationStatus, String notes, int profileId,
            int licenceId) {
        this.examRegistrationId = examRegistrationId;
        this.registrationStatus = registrationStatus;
        this.notes = notes;
        this.profileId = profileId;
        this.licenceId = licenceId;
    }

    public int getExamRegistrationId() {
        return examRegistrationId;
    }

    public void setExamRegistrationId(int examRegistrationId) {
        this.examRegistrationId = examRegistrationId;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Licence getLicence() {
        return licence;
    }

    public void setLicence(Licence licence) {
        this.licence = licence;
    }
}
