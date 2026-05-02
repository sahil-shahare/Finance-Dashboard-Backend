package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Role;
public class AuthResponse {
    private String token,tokenType;
    private Long userId;
    private String username;
    private Role role;
    private AuthResponse(Builder b){this.token=b.token;this.tokenType=b.tokenType;this.userId=b.userId;this.username=b.username;this.role=b.role;}
    public static Builder builder(){return new Builder();}
    public static class Builder {
        private String token,tokenType; private Long userId; private String username; private Role role;
        public Builder token(String v){this.token=v;return this;}
        public Builder tokenType(String v){this.tokenType=v;return this;}
        public Builder userId(Long v){this.userId=v;return this;}
        public Builder username(String v){this.username=v;return this;}
        public Builder role(Role v){this.role=v;return this;}
        public AuthResponse build(){return new AuthResponse(this);}
    }
    public String getToken(){return token;} public String getTokenType(){return tokenType;}
    public Long getUserId(){return userId;} public String getUsername(){return username;} public Role getRole(){return role;}
}
