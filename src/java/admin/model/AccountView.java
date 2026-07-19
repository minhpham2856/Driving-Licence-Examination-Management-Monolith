package admin.model;

import java.sql.Date;
import java.sql.Timestamp;

/** Read/write view cho màn hình Quản lý tài khoản (join [User] + Profile). */
public class AccountView {
    private int userId;
    private String username;
    private String email;
    private String role;          // DB role: Admin/Examiner/...
    private boolean active;
    private Timestamp createdAt;
    private String fullName;
    private String phone;
    private String sex;
    private String govId;
    private String address;
    private Date dateOfBirth;

    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    /** Cho EL ${acc.id} trong JSP. */
    public int getId() { return userId; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public String getRoleCode() { return admin.util.RoleUi.toUiCode(role); }

    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public String getStatus() { return active ? "active" : "inactive"; }

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

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date v) { this.dateOfBirth = v; }

    /** DB không có cột đơn vị -> hiển thị "-". */
    public String getDepartment() { return "-"; }
}
