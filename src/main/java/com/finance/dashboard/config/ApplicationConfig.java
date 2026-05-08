package com.finance.dashboard.config;

import com.finance.dashboard.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class ApplicationConfig {
	private final UserRepository userRepository;

	public ApplicationConfig(UserRepository r) {
		this.userRepository = r;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
