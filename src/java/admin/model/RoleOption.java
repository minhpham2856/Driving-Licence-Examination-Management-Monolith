package admin.model;

public class RoleOption {
    private int roleId;
    private String roleName;
    public int getRoleId() { return roleId; }
    public void setRoleId(int v) { this.roleId = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
    public String getRoleCode() { return admin.util.RoleUi.toUiCode(roleName); }
}
