package Models;

public class CandidateDTO {
    private String sbd;
    private String name;
    private String dob;
    private String cccd;
    private String licenseClass;
    private String phone;

    public CandidateDTO() {}

public CandidateDTO(String sbd, String name, String dob, String cccd, String licenseClass, String phone) {
    this.sbd = sbd;
    this.name = name;
    this.dob = dob;
    this.cccd = cccd;
    this.licenseClass = licenseClass;
    this.phone = phone; // <--- Thêm ở đây
}

    // Sếp tự Generate đầy đủ Getter/Setter cho 5 thuộc tính này nhé
    public String getSbd() { return sbd; }
    public void setSbd(String sbd) { this.sbd = sbd; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getLicenseClass() { return licenseClass; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setLicenseClass(String licenseClass) { this.licenseClass = licenseClass; }
}