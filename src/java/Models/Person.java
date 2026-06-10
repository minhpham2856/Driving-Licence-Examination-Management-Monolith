package Models;

import java.sql.Date;
import java.sql.Timestamp;

public class Person {

    private int id;
    private int userId;
    private String govIdNo;
    private String fullName;
    private Date dateOfBirth;
    private boolean gender; // false = M, true = F
    private String phoneNo;
    private String email;
    private String address;
    private String photoUrl;
    private boolean isWalkIn;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String approvalStatus; // 'Pending', 'Approved', 'Rejected'
    private String rejectionReason;

    public Person() {
    }

    public Person(int id, String govIdNo, String fullName, Date dateOfBirth, boolean gender, String phoneNo, String email, String address, String photoUrl, boolean isWalkIn, Timestamp createdAt, Timestamp updatedAt, String approvalStatus, String rejectionReason) {
        this.id = id;
        this.govIdNo = govIdNo;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.email = email;
        this.address = address;
        this.photoUrl = photoUrl;
        this.isWalkIn = isWalkIn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.approvalStatus = approvalStatus;
        this.rejectionReason = rejectionReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGovIdNo() {
        return govIdNo;
    }

    public void setGovIdNo(String govIdNo) {
        this.govIdNo = govIdNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isIsWalkIn() {
        return isWalkIn;
    }

    public void setIsWalkIn(boolean isWalkIn) {
        this.isWalkIn = isWalkIn;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
