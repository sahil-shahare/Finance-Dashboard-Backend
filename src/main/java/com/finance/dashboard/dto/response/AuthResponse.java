package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.enums.Role;

public class AuthResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private Role role;

    private AuthResponse(Builder b) {
        this.token     = b.token;
        this.tokenType = b.tokenType;
        this.userId    = b.userId;
        this.username  = b.username;
        this.role      = b.role;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String tokenType;
        private Long userId;
        private String username;
        private Role role;
        public Builder token(String t)      { this.token = t; return this; }
        public Builder tokenType(String t)  { this.tokenType = t; return this; }
        public Builder userId(Long u)       { this.userId = u; return this; }
        public Builder username(String u)   { this.username = u; return this; }
        public Builder role(Role r)         { this.role = r; return this; }
        public AuthResponse build()         { return new AuthResponse(this); }
    }

    public String getToken()     { return token; }
    public String getTokenType() { return tokenType; }
    public Long getUserId()      { return userId; }
    public String getUsername()  { return username; }
    public Role getRole()        { return role; }
}
