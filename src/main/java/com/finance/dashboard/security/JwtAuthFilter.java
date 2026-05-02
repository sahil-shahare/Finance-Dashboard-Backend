package com.finance.dashboard.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService){this.jwtUtil=jwtUtil;this.userDetailsService=userDetailsService;}
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,@NonNull HttpServletResponse res,@NonNull FilterChain chain) throws ServletException,IOException {
        String header=req.getHeader("Authorization");
        if(header==null||!header.startsWith("Bearer ")){chain.doFilter(req,res);return;}
        try {
            String token=header.substring(7);
            String username=jwtUtil.extractUsername(token);
            if(username!=null&&SecurityContextHolder.getContext().getAuthentication()==null){
                UserDetails ud=userDetailsService.loadUserByUsername(username);
                if(jwtUtil.isTokenValid(token,ud)){
                    var auth=new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch(Exception e){log.warn("JWT auth error: {}",e.getMessage());}
        chain.doFilter(req,res);
    }
}
