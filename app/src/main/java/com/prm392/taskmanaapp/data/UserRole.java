package com.prm392.taskmanaapp.data;

public class UserRole {
    private int roleId;
    private String roleName; // ADMIN, MANAGER, MEMBER

    public UserRole() {}

    public UserRole(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // Getters and Setters
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
