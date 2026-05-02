package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.Role;
import jakarta.validation.constraints.*;
public class RegisterRequest {
    @NotBlank(message="Username is required") @Size(min=3,max=50) private String username;
    @NotBlank(message="Email is required") @Email private String email;
    @NotBlank(message="Password is required") @Size(min=6) private String password;
    @NotNull(message="Role is required") private Role role;
    public String getUsername(){return username;} public void setUsername(String v){this.username=v;}
    public String getEmail(){return email;} public void setEmail(String v){this.email=v;}
    public String getPassword(){return password;} public void setPassword(String v){this.password=v;}
    public Role getRole(){return role;} public void setRole(Role v){this.role=v;}
}
