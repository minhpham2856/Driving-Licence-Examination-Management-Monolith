package model.exam;

import java.sql.Timestamp;

public class ExamComputer {
    private int id;
    private String computerCode;
    private int areaId;
    private String status; // 'Available', 'InUse', 'Broken', 'Maintenance'
    private Timestamp lastUsedAt;

    public ExamComputer() {
    }

    public ExamComputer(int id, String computerCode, int areaId, String status, Timestamp lastUsedAt) {
        this.id = id;
        this.computerCode = computerCode;
        this.areaId = areaId;
        this.status = status;
        this.lastUsedAt = lastUsedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComputerCode() {
        return computerCode;
    }

    public void setComputerCode(String computerCode) {
        this.computerCode = computerCode;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Timestamp lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
