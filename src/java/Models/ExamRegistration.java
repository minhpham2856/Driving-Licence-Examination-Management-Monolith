package Models;

public class ExamRegistration {
    private int id;
    private String registrationStatus;
    private String notes;
    private int profileId;
    private int licenceId;

    public ExamRegistration() {
    }

    public ExamRegistration(int id, String registrationStatus, String notes, int profileId, int licenceId) {
        this.id = id;
        this.registrationStatus = registrationStatus;
        this.notes = notes;
        this.profileId = profileId;
        this.licenceId = licenceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
