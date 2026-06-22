package Models;

// Maps to table ExamArea. Columns: ExamAreaId, AreaName, AreaType, Capacity, Location.
public class ExamArea {

    private int examAreaId;
    private String areaName;
    private String areaType;
    private int capacity;
    private String location;

    public ExamArea() {
    }

    // Display-only code, e.g. KV-0007
    public String getCode() {
        return String.format("KV-%04d", examAreaId);
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public int getId() {
        return examAreaId;
    }

    public void setId(int examAreaId) {
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
