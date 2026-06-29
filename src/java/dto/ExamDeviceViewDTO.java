package dto;


public class ExamDeviceViewDTO {

    private int examDeviceId;
    private String deviceName;
    private String deviceType;
    private String status;
    private int examAreaId;
    private String areaName;

    public String getCode() {
        return String.format("MT-%04d", examDeviceId);
    }

    public int getId() {
        return examDeviceId;
    }

    public String getName() {
        return deviceName;
    }

    public int getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(int v) {
        this.examDeviceId = v;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String v) {
        this.deviceName = v;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String v) {
        this.deviceType = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int v) {
        this.examAreaId = v;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String v) {
        this.areaName = v;
    }
}
