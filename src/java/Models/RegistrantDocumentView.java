package Models;

/**
 * Trạng thái một loại tài liệu đính kèm hồ sơ thí sinh.
 */
public class RegistrantDocumentView {

    private int documentId;
    private String documentType;
    private String documentUrl;
    private String statusClass;
    private String statusLabel;
    private String notes;
    private String fileName;
    private String fileSizeLabel;

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

    public String getStatusClass() {
        return statusClass;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSizeLabel() {
        return fileSizeLabel;
    }

    public void setFileSizeLabel(String fileSizeLabel) {
        this.fileSizeLabel = fileSizeLabel;
    }
}
