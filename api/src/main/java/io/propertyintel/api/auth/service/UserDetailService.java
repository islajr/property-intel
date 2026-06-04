package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load UserDetails for email: {}", username);

        User user = userRepository.findUserByEmail(username).orElseThrow(() -> {
            log.warn("Database lookup failed. Email not found: {}", username);
            return new BadCredentialsException("Incorrect username or password");
        });

        log.debug("Successfully loaded user details for: {}", username);
        return new UserPrincipal(user);
    }
}
