package io.propertyintel.api.alert.controller;

import io.propertyintel.api.IntegrationTestBase;
import io.propertyintel.api.alert.entity.Alert;
import io.propertyintel.api.alert.repository.AlertRepository;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.auth.entity.enums.UserStatus;
import io.propertyintel.api.auth.repository.UserRepository;
import io.propertyintel.api.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AlertControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        userRepository.deleteAll();

        userA = User.builder()
                .email("usera@example.com")
                .password("$2a$12$eImiTXuWV5j2P19y5/Yk7.5X.j6n2l.j8.oD0y2l0p4UoV1q7m.9.") // bcrypt hash of "password"
                .role("USER")
                .isEmailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        userB = User.builder()
                .email("userb@example.com")
                .password("$2a$12$eImiTXuWV5j2P19y5/Yk7.5X.j6n2l.j8.oD0y2l0p4UoV1q7m.9.")
                .role("USER")
                .isEmailVerified(true)
                .userStatus(UserStatus.ACTIVE)
                .build();

        userA = userRepository.save(userA);
        userB = userRepository.save(userB);

        tokenA = jwtService.generateToken(userA);
    }

    @Test
    void testPostAlertWithoutAuth() throws Exception {
        String requestJson = """
                {
                    "neighbourhood": "Lekki",
                    "maxPriceKobo": 200000000,
                    "minBedrooms": 3,
                    "propertyType": "HOUSE"
                }
                """;

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPostAlertWithAuthSuccess() throws Exception {
        String requestJson = """
                {
                    "neighbourhood": "Lekki",
                    "maxPriceKobo": 200000000,
                    "minBedrooms": 3,
                    "propertyType": "HOUSE"
                }
                """;

        mockMvc.perform(post("/alerts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.neighbourhood").value("Lekki"))
                .andExpect(jsonPath("$.maxPriceKobo").value(200000000))
                .andExpect(jsonPath("$.userId").value(userA.getId().toString()));
    }

    @Test
    void testDeleteAlertOwnedByAnotherUser() throws Exception {
        // Create alert owned by userB
        Alert alertB = Alert.builder()
                .user(userB)
                .neighbourhood("Yaba")
                .maxPriceKobo(100000000L)
                .minBedrooms(2)
                .propertyType("FLAT")
                .alertUnsubscribeToken(UUID.randomUUID())
                .build();
        alertB = alertRepository.save(alertB);

        // Try deleting as userA (tokenA)
        mockMvc.perform(delete("/alerts/" + alertB.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
