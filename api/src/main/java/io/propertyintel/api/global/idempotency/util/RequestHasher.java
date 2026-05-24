package io.propertyintel.api.global.idempotency.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@Slf4j
public class RequestHasher {

    public String hashRequest(String request) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA256");
            byte[] hash = digest.digest(request.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            log.debug("Idempotency hashing error: No such algorithm");
            throw new RuntimeException("Failed to hash request");
        }
    }
}
