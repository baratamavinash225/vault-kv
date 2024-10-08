package com.avinash.vault.kv;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.avinash.vault.kv.auth.AppRoleAuth;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VaultTest {

    private static Vault vault;
    private static final String vaultEndpoint="<>";
    private static final String vaultNamespace="<>";

    @BeforeAll
    static void setup() {
        Map<String,String> env = System.getenv();
        final String roleID = env.get("ROLE_ID");
        final String secretID = env.get("SECRET_ID");
        AppRoleAuth token = new AppRoleAuth(vaultNamespace, roleID, secretID);
        vault = new Vault(URI.create(vaultEndpoint), token, vaultNamespace);
    }

    @Test
    @Order(1)
    @DisplayName("Test addition of empty secret under secret engine")
    public void testCreateEmptySecret() {
        String secretEngineName = "kv-tests";
        String secretName = "empty-secret";
        assertDoesNotThrow(() ->  vault.addSecret(secretEngineName, secretName));
    }

    @Test
    @Order(2)
    @DisplayName("Test addition of path under secret engine with values")
    public void testCreateSecret()  {
        Map<String,String> data = new HashMap<>();
        data.put("mysql.username","root");
        data.put("mysql.password","asd");
        data.put("mysql.version","8");
        Map<String,String> options = new HashMap<>();
        String secretEngineName = "kv-tests";
        String secretName = "database-secrets";
        assertDoesNotThrow(() ->  vault.addSecret(secretEngineName, secretName,data,options));
    }

    @Test
    @Order(3)
    @DisplayName("Test the ability to fetch value for a specific key from a secret under a secret engine")
    public void testGetKeyValueFromSecret() {
        String secretEngineName = "kv-tests";
        String secretName = "database-secrets";
        String key = "mysql.username";
        Map<String,String> secretMap = vault.getSecret("/v1/"+secretEngineName+"/data/"+secretName, key);
        assertNotNull(secretMap.get(key));
        log.info(secretMap.toString());
    }

    @Test
    @Order(4)
    @DisplayName("Test the ability to do add key value using upsert in a secret under secret engine")
    public void testPatchSecret() {
        Map<String,String> options = new HashMap<>();
        Map<String,String> data = new HashMap<>();
        data.put("mysql.db.name","demo");
        String secretEngineName = "kv-tests";
        String secretName = "database-secrets";
        String key = "mysql.db.name";
        assertDoesNotThrow(() ->  vault.patchSecret(secretEngineName, secretName,data,options));
        Map<String,String> secretMap = vault.getSecret("/v1/"+secretEngineName+"/data/"+secretName, key);
        assertEquals("demo",secretMap.get(key));
        log.info(secretMap.toString());
    }

    @Test
    @Order(5)
    @DisplayName("Test destroying a specific version of a secret")
    public void testDestroySecret() {
        String secretEngineName = "kv-tests";
        String secretName = "database-secrets";
        Optional<String> secretVersion = Optional.empty();
        assertDoesNotThrow(() ->  vault.destroySecret(secretEngineName, secretName, secretVersion));
    }

    @Test
    @Order(6)
    @DisplayName("Test destroying an empty secret")
    public void testDestroyEmptySecret() {
        String secretEngineName = "kv-tests";
        String secretName = "empty-secret";
        Optional<String> secretVersion = Optional.empty();
        assertDoesNotThrow(() ->  vault.destroySecret(secretEngineName, secretName, secretVersion));
    }
}
