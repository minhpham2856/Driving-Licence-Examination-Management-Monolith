package shared.model;

public class Document {

    private int documentId;
    private int documentTypeId;
    private String documentUrl;
    private String notes;
    private int profileId;
    private DocumentType documentType;
    private Profile profile;

    public Document() {
    }

    public Document(int documentId, int documentTypeId, String documentUrl, String notes, int profileId) {
        this.documentId = documentId;
        this.documentTypeId = documentTypeId;
        this.documentUrl = documentUrl;
        this.notes = notes;
        this.profileId = profileId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(int documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
