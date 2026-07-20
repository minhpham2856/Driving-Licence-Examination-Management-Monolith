package admin.model;

import java.sql.Date;

public class AccountView {
    private int userId;
    private String username;
    private String email;
    private int roleId;
    private String roleName;     
    private boolean active;      
    private boolean mustChange;
    private String fullName;
    private String phone;
    private boolean sexMale;     
    private String govId;
    private String address;
    private Date dateOfBirth;

    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public int getId() { return userId; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int v) { this.roleId = v; }
    public String getRole() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
    public String getRoleCode() { return admin.util.RoleUi.toUiCode(roleName); }

    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public String getStatus() { return active ? "active" : "inactive"; }
    public boolean isMustChange() { return mustChange; }
    public void setMustChange(boolean v) { this.mustChange = v; }

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }

    public boolean isSexMale() { return sexMale; }
    public void setSexMale(boolean v) { this.sexMale = v; }
    public String getSex() { return sexMale ? "Nam" : "Nữ¯"; }

    public String getGovId() { return govId; }
    public void setGovId(String v) { this.govId = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date v) { this.dateOfBirth = v; }

    public Object getCreatedAt() { return null; }
    public String getDepartment() { return "-"; }
}
