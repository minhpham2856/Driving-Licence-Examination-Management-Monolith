package model;

public class ExamZone {

    private int examZoneId;
    private String zoneName;
    private String location;
    private boolean active;

    public ExamZone() {
    }

    public int getExamZoneId() {
        return examZoneId;
    }

    public void setExamZoneId(int examZoneId) {
        this.examZoneId = examZoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
