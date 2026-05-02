package com.finance.dashboard.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    @Value("${jwt.secret}") private String secretKey;
    @Value("${jwt.expiration}") private long expirationMs;
    private Key signingKey(){return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));}
    public String generateToken(UserDetails u){return Jwts.builder().setSubject(u.getUsername()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+expirationMs)).signWith(signingKey(),SignatureAlgorithm.HS256).compact();}
    public String extractUsername(String token){return parseClaims(token).getSubject();}
    public boolean isTokenValid(String token,UserDetails u){try{return extractUsername(token).equals(u.getUsername())&&!parseClaims(token).getExpiration().before(new Date());}catch(Exception e){log.warn("JWT invalid: {}",e.getMessage());return false;}}
    private Claims parseClaims(String token){return Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token).getBody();}
}
