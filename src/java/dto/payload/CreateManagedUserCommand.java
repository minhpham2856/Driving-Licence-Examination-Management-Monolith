package dto.payload;

public class CreateManagedUserCommand {

    private String fullName;
    private String cccd;
    private String phone;
    private String email;
    private String dob;
    private String sex;
    private String address;
    private String userType;
    private String licenceClass;

    public CreateManagedUserCommand() {
    }

    public CreateManagedUserCommand(String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenceClass) {
        this.fullName = fullName;
        this.cccd = cccd;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.sex = sex;
        this.address = address;
        this.userType = userType;
        this.licenceClass = licenceClass;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }
}
