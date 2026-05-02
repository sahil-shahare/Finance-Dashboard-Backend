package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConstants;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.exception.ConflictException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /** Paginated list — not cached (admin rarely needs sub-millisecond list speed) */
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(userRepository.findAll(pageable), UserResponse::from);
    }

    /**
     * Cached by user ID.
     * User profiles are stable — they change only when an admin explicitly
     * updates them. A 30-minute TTL is safe.
     */
    @Cacheable(value = CacheConstants.USER_BY_ID, key = "#id")
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findUserOrThrow(id));
    }

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Updates user fields and refreshes the cache entry immediately.
     * @CachePut always executes the method AND updates the cache,
     * unlike @Cacheable which skips the method on a cache hit.
     */
    @Transactional
    @CachePut(value = CacheConstants.USER_BY_ID, key = "#id")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email '" + request.getEmail() + "' is already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null)   user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Hard-deletes a user and removes the cache entry so subsequent lookups
     * get a 404 rather than stale data from Redis.
     */
    @Transactional
    @CacheEvict(value = CacheConstants.USER_BY_ID, key = "#id")
    public void deleteUser(Long id) {
        userRepository.delete(findUserOrThrow(id));
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }
}
