package model;

public class ExamArea {

    private int examAreaId;
    private String areaName;
    private String areaType;
    private Integer capacity;
    private String location;
    private int examZoneId;
    private ExamZone examZone;

    public ExamArea() {
    }

    public ExamArea(int examAreaId, String areaName, String areaType, Integer capacity, String location,
            int examZoneId) {
        this.examAreaId = examAreaId;
        this.areaName = areaName;
        this.areaType = areaType;
        this.capacity = capacity;
        this.location = location;
        this.examZoneId = examZoneId;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getExamZoneId() {
        return examZoneId;
    }

    public void setExamZoneId(int examZoneId) {
        this.examZoneId = examZoneId;
    }

    public ExamZone getExamZone() {
        return examZone;
    }

    public void setExamZone(ExamZone examZone) {
        this.examZone = examZone;
    }
}
