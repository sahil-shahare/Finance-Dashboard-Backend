package com.finance.dashboard.model;

import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public User() {}

    private User(Builder b) {
        this.id = b.id;
        this.username = b.username;
        this.email = b.email;
        this.password = b.password;
        this.role = b.role;
        this.status = (b.status != null) ? b.status : UserStatus.ACTIVE;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private Role role;
        private UserStatus status;
        public Builder id(Long id)               { this.id = id; return this; }
        public Builder username(String u)        { this.username = u; return this; }
        public Builder email(String e)           { this.email = e; return this; }
        public Builder password(String p)        { this.password = p; return this; }
        public Builder role(Role r)              { this.role = r; return this; }
        public Builder status(UserStatus s)      { this.status = s; return this; }
        public User build()                      { return new User(this); }
    }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public void setPassword(String password)   { this.password = password; }
    public Role getRole()                      { return role; }
    public void setRole(Role role)             { this.role = role; }
    public UserStatus getStatus()              { return status; }
    public void setStatus(UserStatus status)   { this.status = status; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }

    @Override public String getUsername()      { return username; }
    @Override public String getPassword()      { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isAccountNonLocked()      { return status == UserStatus.ACTIVE; }
    @Override public boolean isEnabled()               { return status == UserStatus.ACTIVE; }
}
