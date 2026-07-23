package admin.model;

/** ExamDevice = máy tính / mô tô (thuộc 1 ExamArea). */
public class DeviceView {
    private int deviceId;
    private String deviceName;
    private String deviceType;    // Máy tính | Mô tô | Mô tô ba bánh
    private boolean active;
    private int areaId;
    private String areaName;
    private String areaType;
    private int zoneId;
    private String zoneName;

    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int v) { this.deviceId = v; }
    public int getId() { return deviceId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String v) { this.deviceName = v; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String v) { this.deviceType = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public String getStatus() { return active ? "active" : "inactive"; }
    public String getStatusText() { return active ? "Hoạt động" : "Bảo trì"; }
    public int getAreaId() { return areaId; }
    public void setAreaId(int v) { this.areaId = v; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String v) { this.areaName = v; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String v) { this.areaType = v; }
    public int getZoneId() { return zoneId; }
    public void setZoneId(int v) { this.zoneId = v; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String v) { this.zoneName = v; }
    public String getCode() { return String.format("MT-%04d", deviceId); }
}
