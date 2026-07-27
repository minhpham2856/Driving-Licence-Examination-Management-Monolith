package examiner.dto;

// Data transfer object for examiner save result views.
public class SaveResultDTO {

    private int entityId;
    private String message;

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
