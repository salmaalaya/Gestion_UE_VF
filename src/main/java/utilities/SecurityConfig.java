package utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Identifiants et JWT, configurés par variables d'environnement :
 * ADMIN_USERNAME (admin), ADMIN_PASSWORD (admin), JWT_SECRET (aléatoire au démarrage).
 */
public final class SecurityConfig {

    private static final Duration TOKEN_DURATION = Duration.ofMinutes(15);
    private static final String USERNAME = env("ADMIN_USERNAME", "admin");
    private static final String PASSWORD = env("ADMIN_PASSWORD", "admin");
    private static final Key KEY = loadKey();

    private SecurityConfig() {}

    public static boolean checkCredentials(String username, String password) {
        return username != null && password != null
                && constantTimeEquals(USERNAME, username)
                && constantTimeEquals(PASSWORD, password);
    }

    public static String issueToken(String username, String issuer) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(TOKEN_DURATION)))
                .signWith(SignatureAlgorithm.HS512, KEY)
                .compact();
    }

    /** Lève une exception si le token est invalide ou expiré. */
    public static void validateToken(String token) {
        Jwts.parser().setSigningKey(KEY).parseClaimsJws(token);
    }

    private static Key loadKey() {
        byte[] secret;
        String configured = System.getenv("JWT_SECRET");
        if (configured == null || configured.isBlank()) {
            secret = new byte[64];
            new SecureRandom().nextBytes(secret);
        } else {
            secret = configured.getBytes(StandardCharsets.UTF_8);
        }
        return new SecretKeySpec(sha512(secret), "HmacSHA512"); // clé de 512 bits pour HS512
    }

    private static byte[] sha512(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-512").digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
