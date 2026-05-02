package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.UserStatus;
import java.time.LocalDateTime;
public class UserResponse {
    private Long id; private String username,email; private Role role; private UserStatus status; private LocalDateTime createdAt;
    private UserResponse(Builder b){this.id=b.id;this.username=b.username;this.email=b.email;this.role=b.role;this.status=b.status;this.createdAt=b.createdAt;}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private Long id; private String username,email; private Role role; private UserStatus status; private LocalDateTime createdAt;
        public Builder id(Long v){this.id=v;return this;} public Builder username(String v){this.username=v;return this;}
        public Builder email(String v){this.email=v;return this;} public Builder role(Role v){this.role=v;return this;}
        public Builder status(UserStatus v){this.status=v;return this;} public Builder createdAt(LocalDateTime v){this.createdAt=v;return this;}
        public UserResponse build(){return new UserResponse(this);}
    }
    public static UserResponse from(User u){return UserResponse.builder().id(u.getId()).username(u.getUsername()).email(u.getEmail()).role(u.getRole()).status(u.getStatus()).createdAt(u.getCreatedAt()).build();}
    public Long getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;}
    public Role getRole(){return role;} public UserStatus getStatus(){return status;} public LocalDateTime getCreatedAt(){return createdAt;}
}
