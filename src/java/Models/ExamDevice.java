package Models;

public class ExamDevice {
    private int id;
    private int areaId;
    private String deviceType;
    private String deviceName;
    private String status; // 'Operational', 'Maintenance', etc.

    public ExamDevice() {
    }

    public ExamDevice(int id, int areaId, String deviceType, String deviceName, String status) {
        this.id = id;
        this.areaId = areaId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
