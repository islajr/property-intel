package io.propertyintel.api.global.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @Test
    void testHashTokenSuccess() {
        String rawToken = "my-secret-token-123";
        String hashed = SecurityUtils.hashToken(rawToken);

        assertNotNull(hashed);
        // SHA-256 hex string should be 64 characters long
        assertEquals(64, hashed.length());
        
        // Hashing the same input should return the identical hash (deterministic)
        String hashedAgain = SecurityUtils.hashToken(rawToken);
        assertEquals(hashed, hashedAgain);

        // Verification with an expected SHA-256 hex output for a known value
        // "test" hashed via SHA-256 is "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        String expectedHashForTest = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
        assertEquals(expectedHashForTest, SecurityUtils.hashToken("test"));
    }

    @Test
    void testHashTokenWithNullThrowsException() {
        assertThrows(NullPointerException.class, () -> SecurityUtils.hashToken(null));
    }
}
