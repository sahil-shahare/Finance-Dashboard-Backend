package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.model.enums.UserStatus;
import jakarta.validation.constraints.*;
public class UpdateUserRequest {
    @Email private String email;
    private Role role;
    private UserStatus status;
    @Size(min=6) private String password;
    public String getEmail(){return email;} public void setEmail(String v){this.email=v;}
    public Role getRole(){return role;} public void setRole(Role v){this.role=v;}
    public UserStatus getStatus(){return status;} public void setStatus(UserStatus v){this.status=v;}
    public String getPassword(){return password;} public void setPassword(String v){this.password=v;}
}
