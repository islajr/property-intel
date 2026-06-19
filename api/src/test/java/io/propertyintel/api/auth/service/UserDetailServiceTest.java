package io.propertyintel.api.auth.service;

import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailService userDetailService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .isEmailVerified(true)
                .build();
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = userDetailService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        
        verify(userRepository).findUserByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsernameNotFoundThrowsBadCredentialsException() {
        when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> 
                userDetailService.loadUserByUsername("unknown@example.com")
        );

        verify(userRepository).findUserByEmail("unknown@example.com");
    }
}
