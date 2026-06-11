package Models;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MyExamDetailView {

    private int registrationId;
    private String licenceLabel;
    private String sessionCode;
    private Date examDate;
    private String gatherTimeLabel;
    private String roomLabel;
    private String machineLabel;
    /** SBD — chỉ nạp từ bảng Candidate, không từ ExamRegistration. */
    private String sbd;
    private boolean qrAvailable;
    private boolean paymentPending;
    private boolean cancelled;
    private List<MyExamScoreSection> scoreSections = new ArrayList<>();

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getLicenceLabel() {
        return licenceLabel;
    }

    public void setLicenceLabel(String licenceLabel) {
        this.licenceLabel = licenceLabel;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getGatherTimeLabel() {
        return gatherTimeLabel;
    }

    public void setGatherTimeLabel(String gatherTimeLabel) {
        this.gatherTimeLabel = gatherTimeLabel;
    }

    public String getRoomLabel() {
        return roomLabel;
    }

    public void setRoomLabel(String roomLabel) {
        this.roomLabel = roomLabel;
    }

    public String getMachineLabel() {
        return machineLabel;
    }

    public void setMachineLabel(String machineLabel) {
        this.machineLabel = machineLabel;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public boolean isQrAvailable() {
        return qrAvailable;
    }

    public void setQrAvailable(boolean qrAvailable) {
        this.qrAvailable = qrAvailable;
    }

    public List<MyExamScoreSection> getScoreSections() {
        return scoreSections;
    }

    public void setScoreSections(List<MyExamScoreSection> scoreSections) {
        this.scoreSections = scoreSections;
    }

    public boolean isPaymentPending() {
        return paymentPending;
    }

    public void setPaymentPending(boolean paymentPending) {
        this.paymentPending = paymentPending;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
