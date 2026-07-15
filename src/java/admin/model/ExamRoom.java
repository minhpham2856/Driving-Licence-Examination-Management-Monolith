package admin.model;

import java.sql.Timestamp;

/**
 * Maps to table ExamRoom (created by create-exam-room.sql).
 * A room belongs to an ExamArea. Display helpers feed the existing JSP.
 */
public class ExamRoom {

    private int examRoomId;
    private String roomName;
    private String roomType;   // 'theory' | 'practical'
    private Integer capacity;
    private String floor;
    private String status;     // 'active' | 'maintenance' | 'inactive'
    private int examAreaId;

    private Timestamp createdAt;
    private Integer createdByUserId;
    private Timestamp updatedAt;
    private Integer updatedByUserId;

    // joined from ExamArea
    private String areaName;
    private int computerCount; // devices belonging to this room (COUNT from ExamDevice)

    /** Display code shown in the UI, e.g. PT-0001. JSP uses ${room.code}. */
    public String getCode() {
        return String.format("PT-%04d", examRoomId);
    }

    /** Area display code KV-0001 (matches ExamArea.getCode()). JSP uses ${room.areaCode}. */
    public String getAreaCode() {
        return String.format("KV-%04d", examAreaId);
    }

    // JSP convenience aliases (exam-room.jsp uses room.id / room.name / room.type / room.computerCount)
    public int getId() { return examRoomId; }
    public String getName() { return roomName; }
    public String getType() { return roomType; }
    public int getComputerCount() { return computerCount; }
    public void setComputerCount(int computerCount) { this.computerCount = computerCount; }

    public int getExamRoomId() { return examRoomId; }
    public void setExamRoomId(int examRoomId) { this.examRoomId = examRoomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getExamAreaId() { return examAreaId; }
    public void setExamAreaId(int examAreaId) { this.examAreaId = examAreaId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Integer getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Integer v) { this.createdByUserId = v; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Integer v) { this.updatedByUserId = v; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
}
