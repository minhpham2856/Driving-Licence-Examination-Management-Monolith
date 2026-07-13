package shared.model;

public class DocumentType {

    private int documentTypeId;
    private String type;

    public DocumentType() {
    }

    public DocumentType(int documentTypeId, String type) {
        this.documentTypeId = documentTypeId;
        this.type = type;
    }

    public int getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(int documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
