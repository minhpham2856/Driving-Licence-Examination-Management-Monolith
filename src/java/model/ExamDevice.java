package model;
public class ExamDevice {
    private int examDeviceId;
    private String deviceName;
    private String deviceType;
    private boolean isActive;
    private int examAreaId;
    public ExamDevice() {
    }
    public ExamDevice(int examDeviceId, String deviceName, String deviceType, boolean isActive, int examAreaId) {
        this.examDeviceId = examDeviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.isActive = isActive;
        this.examAreaId = examAreaId;
    }
    public int getExamDeviceId() {
        return examDeviceId;
    }
    public void setExamDeviceId(int examDeviceId) {
        this.examDeviceId = examDeviceId;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public int getExamAreaId() {
        return examAreaId;
    }
    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }
}
