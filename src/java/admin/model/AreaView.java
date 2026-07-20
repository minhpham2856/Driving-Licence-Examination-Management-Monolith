package admin.model;

/** ExamArea = phòng thi / sân thi (thuộc 1 ExamZone). */
public class AreaView {
    private int areaId;
    private String areaName;
    private String areaType;      // Phòng thủ tục | Phòng thi | Sân thi
    private Integer capacity;     // nullable
    private String location;
    private int zoneId;
    private String zoneName;
    private int deviceCount;

    public int getAreaId() { return areaId; }
    public void setAreaId(int v) { this.areaId = v; }
    public int getId() { return areaId; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String v) { this.areaName = v; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String v) { this.areaType = v; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer v) { this.capacity = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public int getZoneId() { return zoneId; }
    public void setZoneId(int v) { this.zoneId = v; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String v) { this.zoneName = v; }
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int v) { this.deviceCount = v; }
    public String getCode() { return String.format("PT-%04d", areaId); }
    public String getCapacityText() { return capacity == null ? "-" : String.valueOf(capacity); }
}
