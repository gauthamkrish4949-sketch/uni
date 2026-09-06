package com.collegestore.unistore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role;
    private boolean isAuthorized = true;

    public User() {}

    public User(String username, String password, String role, boolean isAuthorized) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAuthorized = isAuthorized;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAuthorized() { return isAuthorized; }
    public void setAuthorized(boolean isAuthorized) { this.isAuthorized = isAuthorized; }
    public void setIsAuthorized(boolean isAuthorized) { this.isAuthorized = isAuthorized; }
}