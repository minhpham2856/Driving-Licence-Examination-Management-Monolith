package Models;

import java.sql.Timestamp;


public class AccountView {

    private int userId;
    private String username;
    private String email;
    private String role;        // DB role string (Admin/Examiner/...)
    private boolean active;     // [User].Status
    private Timestamp createdAt;

    // from Profile
    private String fullName;
    private String phone;
    private String sex;
    private String govId;
    private String address;
    private java.sql.Date dateOfBirth;

    // ---- JSP aliases (accounts.jsp uses acc.id / fullName / username / email / phone / role / status / createdAt / department) ----
    public int getId() { return userId; }
    public String getRoleCode() { return Constants.RoleUi.toUiCode(role); }
    /** accounts.jsp reads acc.role for the badge -> give it the UI code. */
    public String getRoleUi() { return getRoleCode(); }
    public String getStatus() { return active ? "active" : "inactive"; }
    public String getDepartment() { return "-”"; } // no column in DB

    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getSex() { return sex; }
    public void setSex(String v) { this.sex = v; }
    public String getGovId() { return govId; }
    public void setGovId(String v) { this.govId = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public java.sql.Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(java.sql.Date v) { this.dateOfBirth = v; }
}
