package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.RegisterRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.exception.ConflictException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.UserStatus;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;

	public AuthService(UserRepository r, PasswordEncoder p, JwtUtil j, AuthenticationManager a) {
		this.userRepository = r;
		this.passwordEncoder = p;
		this.jwtUtil = j;
		this.authenticationManager = a;
	}

	@Transactional
	public AuthResponse register(RegisterRequest req) {
		if (userRepository.existsByUsername(req.getUsername()))
			throw new ConflictException("Username '" + req.getUsername() + "' is already taken");
		if (userRepository.existsByEmail(req.getEmail()))
			throw new ConflictException("Email '" + req.getEmail() + "' is already registered");
		User user = User.builder().username(req.getUsername()).email(req.getEmail())
				.password(passwordEncoder.encode(req.getPassword())).role(req.getRole()).status(UserStatus.ACTIVE)
				.build();
		userRepository.save(user);
		return build(user, jwtUtil.generateToken(user));
	}

	public AuthResponse login(LoginRequest req) {
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
		User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
		return build(user, jwtUtil.generateToken(user));
	}

	private AuthResponse build(User u, String token) {
		return AuthResponse.builder().token(token).tokenType("Bearer").userId(u.getId()).username(u.getUsername())
				.role(u.getRole()).build();
	}
}
