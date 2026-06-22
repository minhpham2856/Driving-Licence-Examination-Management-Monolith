package Models;

import java.sql.Timestamp;

public class Profile {

    private int profileId;
    private String fullName;
    private Timestamp dateOfBirth;
    private String phoneNumber;
    private String sex;
    private String governmentIdNumber;
    private String address;
    private int userId;

    public Profile() {
    }

    public Profile(int profileId, String fullName, Timestamp dateOfBirth, String phoneNumber, String sex, String governmentIdNumber, String address, int userId) {
        this.profileId = profileId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.governmentIdNumber = governmentIdNumber;
        this.address = address;
        this.userId = userId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getGovernmentIdNumber() {
        return governmentIdNumber;
    }

    public void setGovernmentIdNumber(String governmentIdNumber) {
        this.governmentIdNumber = governmentIdNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Backwards compatibility helpers
    public String getGovIdNo() {
        return governmentIdNumber;
    }

    public void setGovIdNo(String govIdNo) {
        this.governmentIdNumber = govIdNo;
    }

    public String getPhoneNo() {
        return phoneNumber;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNumber = phoneNo;
    }

    public boolean isGender() {
        if (sex == null) {
            return false;
        }
        String s = sex.trim();
        return !(s.equalsIgnoreCase("Nam") || s.equalsIgnoreCase("Male") || s.equals("M"));
    }

    public void setGender(boolean gender) {
        this.sex = gender ? "Nữ" : "Nam";
    }
}
