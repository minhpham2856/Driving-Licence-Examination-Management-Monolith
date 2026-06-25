package model.exam;

import java.sql.Timestamp;

/**
 * Maps to table ExamDevice: ExamDeviceId, DeviceName, DeviceType, Status, ExamAreaId + audit.
 */
public class ExamDevice {

    private int examDeviceId;
    private String deviceName;
    private String deviceType;
    private String status;       // 'active' | 'maintenance' | 'broken'
    private int examAreaId;

    private Timestamp createdAt;
    private Integer createdByUserId;
    private Timestamp updatedAt;
    private Integer updatedByUserId;

    // joined
    private String areaName;

    /**
     * Display code shown in the UI, e.g. MT-0001. JSP uses ${dev.code}.
     */
    public String getCode() {
        return String.format("MT-%04d", examDeviceId);
    }

    public String getAreaCode() {
        return String.format("KV-%04d", examAreaId);
    }

    // JSP convenience aliases
    public int getId() {
        return examDeviceId;
    }

    public String getName() {
        return deviceName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer v) {
        this.createdByUserId = v;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Integer v) {
        this.updatedByUserId = v;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
