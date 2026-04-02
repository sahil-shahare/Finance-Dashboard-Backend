package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.UserStatus;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    private UserResponse(Builder b) {
        this.id        = b.id;
        this.username  = b.username;
        this.email     = b.email;
        this.role      = b.role;
        this.status    = b.status;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private Role role;
        private UserStatus status;
        private LocalDateTime createdAt;
        public Builder id(Long id)                   { this.id = id; return this; }
        public Builder username(String u)            { this.username = u; return this; }
        public Builder email(String e)               { this.email = e; return this; }
        public Builder role(Role r)                  { this.role = r; return this; }
        public Builder status(UserStatus s)          { this.status = s; return this; }
        public Builder createdAt(LocalDateTime c)    { this.createdAt = c; return this; }
        public UserResponse build()                  { return new UserResponse(this); }
    }

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Long getId()            { return id; }
    public String getUsername()    { return username; }
    public String getEmail()       { return email; }
    public Role getRole()          { return role; }
    public UserStatus getStatus()  { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
