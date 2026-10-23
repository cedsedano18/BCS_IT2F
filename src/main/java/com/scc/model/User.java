
package com.scc.model;

public class User {

    private final int userId;
    private final String username;
    private final String fullName;
    private final String role;
    private final String status;

    public User(
            int userId,
            String username,
            String fullName,
            String role,
            String status) {

        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
