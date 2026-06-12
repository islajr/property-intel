package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.dto.AuthResult;
import io.propertyintel.api.auth.dto.LoginRequest;
import io.propertyintel.api.auth.dto.RegisterRequest;
import io.propertyintel.api.auth.entity.RefreshToken;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.UserPrincipal;
import io.propertyintel.api.auth.entity.enums.UserStatus;
import io.propertyintel.api.auth.repository.UserRepository;
import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    @Value("${accesstoken.expiry.seconds}")
    private Integer accessTokenExpirySeconds;

    @Transactional
    public AuthResult login(LoginRequest loginRequest) {
        log.info("Login request from user: {}", loginRequest.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        if (!authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            log.warn("Authentication failed for user: {}", loginRequest.email());
            throw new UnauthorizedException("Authentication failed for user: " + loginRequest.email());
        }

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(user.getUser());

        log.info("Generated access token for user: {}", user.getEmail());

        String refreshToken = refreshTokenService.generateRefreshToken(user.getUser(), false);
        log.info("Generated refresh token for user: {}", user.getEmail());

        log.info("Successfully logged in user: {}", user.getEmail());

        return new AuthResult(accessToken, refreshToken, accessTokenExpirySeconds);
    }

    @Transactional
    public AuthResult register(RegisterRequest registerRequest) {

        log.info("Registration request from user: {}", registerRequest.email());

        if (userRepository.existsByEmail(registerRequest.email())) {
            log.debug("Duplicate e-mail registration request from user {}", registerRequest.email());
            throw new BadRequestException("User with email " + registerRequest.email() + " already exists");
        }

        User user = User.builder()
                .role("USER")   // TODO: see to 'ROLE' business and control
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .isEmailVerified(false)
                .userStatus(UserStatus.UNVERIFIED)
                .build();

        userRepository.save(user);
        log.debug("Created new user {}", user.getEmail());

        // asynchronous job to issue and send verification e-mails
        emailVerificationService.issueAndSendVerificationEmail(user);

        String accessToken = jwtService.generateToken(user);
        log.debug("Generated access token for new user: {}", user.getEmail());

        String refreshToken = refreshTokenService.generateRefreshToken(user, true);
        log.debug("Generated refresh token for new user: {}", user.getEmail());

        log.info("Successfully registered user: {}", user.getEmail());

        return new AuthResult(accessToken, refreshToken, accessTokenExpirySeconds);
    }

    @Transactional
    public AuthResult refresh(String refreshToken) {

        /*
        * Refreshes access token with the refresh token as long as the latter remains valid.
        * Refreshes refresh token also every 15 minutes
        * */

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new UnauthorizedException("Refresh token not found.");
        }

        RefreshToken storedToken = refreshTokenService.validateToken(refreshToken);

        UserPrincipal userPrincipal =
                (UserPrincipal) userDetailsService.loadUserByUsername(storedToken.getUser().getEmail());

        log.info("Refresh token attempt from user: {}", userPrincipal.getEmail());
        String accessToken = jwtService.generateToken(userPrincipal.getUser());
        log.debug("Refreshed access token for user: {}", userPrincipal.getEmail());

        String newRefreshToken = refreshTokenService.generateRefreshToken(userPrincipal.getUser(), false);
        log.debug("Generated refresh token for logged-in user: {}", userPrincipal.getEmail());

        log.info("Successfully refreshed access and refresh tokens for user: {}", userPrincipal.getEmail());

        return new AuthResult(accessToken, newRefreshToken, accessTokenExpirySeconds);
    }
}