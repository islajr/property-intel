package io.propertyintel.api.auth.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
@Slf4j
public class RSAKeyProperties {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public RSAKeyProperties() {
        this.publicKey = loadPublicKey();
        this.privateKey = loadPrivateKey();
    }

    private RSAPublicKey loadPublicKey() {
        try {
            String key = Files.readString(
                    new ClassPathResource("keys/public.pem")    // TODO: should extract into secrets
                            .getFile()
                            .toPath()
            );

            key = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(decoded);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            log.debug("Successfully loaded public key: {}", key);
            return (RSAPublicKey) factory.generatePublic(spec);



        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.debug("Failed to load public key", e);
            throw new RuntimeException("Failed to authenticate user", e);
        }
    }

    private RSAPrivateKey loadPrivateKey() {
        try {
            String key = Files.readString(
                    new ClassPathResource("keys/private.pem")   // TODO: should extract into secrets
                            .getFile()
                            .toPath()
            );

            key = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(decoded);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            log.debug("Successfully loaded private key");
            return (RSAPrivateKey) factory.generatePrivate(spec);


        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.debug("Failed to load private key", e);
            throw new RuntimeException("Failed to authenticate user", e);
        }
    }
}
