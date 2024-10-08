package com.avinash.vault.kv.auth;

import com.avinash.vault.kv.exceptions.VaultInitializationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@AllArgsConstructor
public class TokenAuth implements VaultAuth {

    private String token;

    public static VaultAuth configuredAuth(Map<String, ?> configs) {
        String token = null;
        if (configs.containsKey("token")) {
            token = (String) configs.get("token");
            log.info("Got vault token from config : ************");
        }
        if (token == null || token.isBlank()) {
            throw new VaultInitializationException("Vault token must be supplied." +
                " Token can be set in worker config against config.providers.vault.param.token");
        }
        return new TokenAuth(token);
    }

    @Override
    public String getToken() {
        return token;
    }
}
