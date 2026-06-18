package Models;

public class Document {
    private int documentId;
    private String documentType;
    private String documentUrl;
    private String notes;
    private int profileId;

    public Document() {
    }

    public Document(int documentId, String documentType, String documentUrl, String notes, int profileId) {
        this.documentId = documentId;
        this.documentType = documentType;
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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
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
}
