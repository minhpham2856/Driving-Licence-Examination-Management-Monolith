package dto;

public class RecentActivityDTO {

    private String timestamp;
    private String username;
    private String action;
    private String module;
    private String recordId;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getIpAddress() {
        return "—";
    }

    public String getStatus() {
        return "Thành công";
    }

    public String getStatusKey() {
        return "success";
    }
}
