package org.itson.ecommerce_e2.services.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

class PasswordUtil {

    // Salt estático del proyecto — no cambiar una vez que hay usuarios en BD
    private static final String SALT = "nexusgames_salt_2026";

    private PasswordUtil() {
    }

    static String hash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salted = SALT + plainPassword;
            byte[] digest = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    static boolean matches(String plainPassword, String hashedPassword) {
        return hash(plainPassword).equals(hashedPassword);
    }
}
