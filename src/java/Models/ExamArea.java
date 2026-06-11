package Models;

public class ExamArea {
    private int id;
    private String areaName;
    private String areaType; // 'Room', 'Ground', 'Road'
    private int capacity;
    private String location;
    private boolean isActive;

    public ExamArea() {
    }

    public ExamArea(int id, String areaName, String areaType, int capacity, String location, boolean isActive) {
        this.id = id;
        this.areaName = areaName;
        this.areaType = areaType;
        this.capacity = capacity;
        this.location = location;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
