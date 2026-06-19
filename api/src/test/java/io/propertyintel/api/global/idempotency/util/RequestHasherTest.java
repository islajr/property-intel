package io.propertyintel.api.global.idempotency.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHasherTest {

    private final RequestHasher requestHasher = new RequestHasher();

    @Test
    void testHashRequestIsDeterministic() {
        String input = "{\"email\":\"test@example.com\",\"password\":\"pwd\"}";
        String hash1 = requestHasher.hashRequest(input);
        String hash2 = requestHasher.hashRequest(input);

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 hex is 64 chars
        assertEquals(hash1, hash2);
    }

    @Test
    void testHashRequestDifferentInputsProduceDifferentHashes() {
        String input1 = "input-1";
        String input2 = "input-2";

        String hash1 = requestHasher.hashRequest(input1);
        String hash2 = requestHasher.hashRequest(input2);

        assertNotEquals(hash1, hash2);
    }
}
