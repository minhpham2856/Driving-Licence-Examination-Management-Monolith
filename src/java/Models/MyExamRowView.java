package Models;

import java.sql.Date;
import java.sql.Time;

public class MyExamRowView {

    private int registrationId;
    private int examSessionId;
    private String title;
    private Date examDate;
    private String licenceCode;
    private String examTypeName;
    private String sbd;
    private String roomLabel;
    private String location;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private String statusLabel;
    private String statusClass;
    private boolean pendingRow;
    private boolean cancelled;

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getLicenceCode() {
        return licenceCode;
    }

    public void setLicenceCode(String licenceCode) {
        this.licenceCode = licenceCode;
    }

    public String getExamTypeName() {
        return examTypeName;
    }

    public void setExamTypeName(String examTypeName) {
        this.examTypeName = examTypeName;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public String getRoomLabel() {
        return roomLabel;
    }

    public void setRoomLabel(String roomLabel) {
        this.roomLabel = roomLabel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Time getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Time shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public Time getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(Time shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getStatusClass() {
        return statusClass;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public boolean isPendingRow() {
        return pendingRow;
    }

    public void setPendingRow(boolean pendingRow) {
        this.pendingRow = pendingRow;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
