package io.propertyintel.api.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class SecurityUtils {

    /*
    * Hash and encode any provided string (token) using SHA-256*/
    public static String hashToken(String rawToken) {
        try {
            MessageDigest message = MessageDigest.getInstance("SHA-256");
            byte[] hashedMessage = message.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(hashedMessage));

        } catch (NoSuchAlgorithmException e) {
            log.warn("Throwing exception due to unrecognized hashing algorithm");
            throw new RuntimeException("Hashing algorithm: SHA-256 not available");
        }

    }
}
