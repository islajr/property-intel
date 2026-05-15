package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username).orElseThrow(
                () -> new BadCredentialsException("Incorrect username or password"));

        return new UserPrincipal(user);
    }
}
