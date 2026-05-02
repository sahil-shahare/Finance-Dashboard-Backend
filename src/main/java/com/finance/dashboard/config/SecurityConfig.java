package com.finance.dashboard.config;

import com.finance.dashboard.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Public ────────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── Users (Admin only) ────────────────────────────────
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ── Transactions ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET,    "/api/transactions/**").authenticated()
                        .requestMatchers(HttpMethod.POST,   "/api/transactions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/transactions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").hasRole("ADMIN")

                        // ── Dashboard ─────────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/summary").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/trends").hasAnyRole("ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/categories").hasAnyRole("ANALYST", "ADMIN")

                        // ── Cache management (Admin only) ────────────────────
                        .requestMatchers("/api/cache/**").hasRole("ADMIN")

                        // ── Payments ──────────────────────────────────────────
                        // create-order and verify: any authenticated user
                        .requestMatchers(HttpMethod.POST, "/api/payments/create-order").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/payments/verify").authenticated()
                        // my history: any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/payments/my").authenticated()
                        // single payment lookup: any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}").authenticated()
                        // all payments + revenue: admin only (also enforced via @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/payments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/revenue").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
