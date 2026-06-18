package Models;


public class ExamDeviceView {

    private int examDeviceId;
    private String deviceName;
    private String deviceType;
    private String status;      // 
    private int examRoomId;     // 
    private int examAreaId;
    private String roomName;
    private String areaName;

    public String getCode() { return String.format("MT-%04d", examDeviceId); }
    public String getRoomCode() { return examRoomId > 0 ? String.format("PT-%04d", examRoomId) : "â€”"; }

    // aliases used by the JSP
    public int getId() { return examDeviceId; }
    public String getName() { return deviceName; }

    public int getExamDeviceId() { return examDeviceId; }
    public void setExamDeviceId(int v) { this.examDeviceId = v; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String v) { this.deviceName = v; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String v) { this.deviceType = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public int getExamRoomId() { return examRoomId; }
    public void setExamRoomId(int v) { this.examRoomId = v; }

    public int getExamAreaId() { return examAreaId; }
    public void setExamAreaId(int v) { this.examAreaId = v; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String v) { this.roomName = v; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String v) { this.areaName = v; }
}
