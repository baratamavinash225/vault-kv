package com.avinash.vault.kv;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Testing class to Test the VaultSecretProvider
 * Vault URL and namespace to be passed to make the UT's Succesful.
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultSecretProviderTest {

    private static VaultSecretProvider vaultSecretProvider;

    private static String ROLE_ID;
    private static String SECRET_ID;
    private static String ROLE_ID_ENCRYPTED;
    private static String SECRET_ID_ENCRYPTED;

    @BeforeAll
    static void setup() {
        vaultSecretProvider = new VaultSecretProvider();
        Map env = System.getenv();
        ROLE_ID = (String) env.get("ROLE_ID");
        SECRET_ID = (String) env.get("SECRET_ID");
        ROLE_ID_ENCRYPTED = (String) env.get("ROLE_ID_ENCRYPTED");
        SECRET_ID_ENCRYPTED = (String) env.get("SECRET_ID_ENCRYPTED");
    }

    @Test
    @Order(1)
    @DisplayName("Testing the Default configuration")
    public void testDefaultConfiguration() {
        Map<String, ?> cfg = new HashMap() {{
            put("url", "<>");
            put("namespace", "<>");
            put("auth.mechanism" , "APP_ROLE_AUTH");
            put("role.id", ROLE_ID);
            put("secret.id", SECRET_ID);
        }};
        assertDoesNotThrow(() ->
                vaultSecretProvider.configure(cfg));

    }

    @Test
    @Order(2)
    @DisplayName("Testing with the encrypted false")
    public void testencryptedFalseConfiguration() {
        Map<String, ?> cfg = new HashMap() {{
            put("url", "<>");
            put("namespace", "<>");
            put("auth.mechanism" , "APP_ROLE_AUTH");
            put("role.id", ROLE_ID);
            put("secret.id", SECRET_ID);
        }};
        assertDoesNotThrow(() ->
            vaultSecretProvider.configure(cfg)
        );
    }

    @Test
    @Order(3)
    @DisplayName("Testing with the encrypted true")
    public void testencryptedTrueConfiguration() {
        Map<String, ?> cfg = new HashMap() {{
            put("url", "<>");
            put("namespace", "<>");
            put("auth.mechanism" , "APP_ROLE_AUTH");
            put("role.id", ROLE_ID);
            put("secret.id", SECRET_ID_ENCRYPTED);
        }};
        assertDoesNotThrow(() -> {
            vaultSecretProvider.configure(cfg);
        }) ;
    }

    @Test
    @Order(4)
    @DisplayName("Testing with the Encrypted configuration and no certificates passed, should throw error")
    public void testEncryptedConfiguration() {
        Map<String, ?> cfg = new HashMap() {{
            put("url", "<>");
            put("namespace", "<>");
            put("auth.mechanism" , "APP_ROLE_AUTH");
            put("role.id", ROLE_ID_ENCRYPTED);
            put("secret.id", SECRET_ID_ENCRYPTED);
        }};
            assertDoesNotThrow(() -> {
                vaultSecretProvider.configure(cfg);
            });

    }
}
