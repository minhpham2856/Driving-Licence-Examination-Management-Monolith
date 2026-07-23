package admin.model;

/** ExamZone = khuôn viên/địa điểm thi. */
public class ZoneView {
    private int zoneId;
    private String zoneName;
    private String location;
    private boolean active;
    private int areaCount;   // số phòng/sân thuộc zone (để hiển thị)

    public int getZoneId() { return zoneId; }
    public void setZoneId(int v) { this.zoneId = v; }
    public int getId() { return zoneId; }              // cho ${z.id}
    public String getZoneName() { return zoneName; }
    public void setZoneName(String v) { this.zoneName = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public String getStatus() { return active ? "active" : "inactive"; }
    public int getAreaCount() { return areaCount; }
    public void setAreaCount(int v) { this.areaCount = v; }
    public String getCode() { return String.format("KV-%04d", zoneId); }
}
