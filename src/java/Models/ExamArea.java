package Models;

import java.sql.Timestamp;

/**
 * Maps to table ExamArea.
 * Columns: ExamAreaId, AreaName, AreaType, Capacity, Location + audit fields.
 */
public class ExamArea {

    private int examAreaId;
    private String areaName;
    private String areaType;   // e.g. "LÃ½ thuyáº¿t", "Thá»±c hÃ nh", "Há»—n há»£p"
    private int capacity;
    private String location;
    private Timestamp createdAt;
    private Integer createdByUserId;
    private Timestamp updatedAt;
    private Integer updatedByUserId;

    public ExamArea() {
    }

    /** Display-only code, e.g. KV-0007 */
    public String getCode() {
        return String.format("KV-%04d", examAreaId);
    }

    public int getExamAreaId() { return examAreaId; }
    public void setExamAreaId(int examAreaId) { this.examAreaId = examAreaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer createdByUserId) { this.createdByUserId = createdByUserId; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Integer updatedByUserId) { this.updatedByUserId = updatedByUserId; }
}
