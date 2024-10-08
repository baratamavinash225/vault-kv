package com.avinash.vault.kv;

import com.avinash.vault.kv.Vault.VaultBuilder;
import com.avinash.vault.kv.auth.AppRoleAuth;
import com.avinash.vault.kv.auth.TokenAuth;
import com.avinash.vault.kv.auth.VaultAuth;
import com.avinash.vault.kv.exceptions.VaultInitializationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.provider.ConfigProvider;
import org.apache.kafka.common.config.provider.FileConfigProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation inline with {@link FileConfigProvider} that represents a Properties file which
 * has all property keys and values stored as a clear text.
 * This {@link VaultSecretProvider} implementation of {@link ConfigProvider} is capable of pulling
 * secrets from HashiCorp Vault(Any vault compliant with Hashi Corp Vault)
 */
@Slf4j
public class VaultSecretProvider implements ConfigProvider {

    private SecretProvider vault;

    @Override
    public ConfigData get(String path) {
        log.info("VaultSecretProvider get({}) called", path);
        return new ConfigData(vault.getSecret(path));
    }

    @Override
    public ConfigData get(String path, Set<String> keys) {
        log.info("VaultSecretProvider get({},{}) called", path, keys);
        Map<String, String> res = new HashMap<>();
        for (String key : keys) {
            res.put(key, vault.getSecret(path, key).get(key));
        }
        return new ConfigData(res);
    }

    @Override
    public void close() {
        log.info("VaultSecretProvider closed");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.info("VaultSecretProvider is initializing");
        URI uri = URI.create("<>");
        String namespace;

        //Overwrite values if required
        if (configs.containsKey("url")) {
            String url = (String) configs.get("url");
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                throw new VaultInitializationException("URL provided for vault config is not a valid URI : " + url);
            }
            log.info("Got vault url from config : {}", uri);
        } else {
            log.warn("No url is provided in config. Default {} url will be used.", uri);
        }
        if (configs.containsKey("namespace")) {
            namespace = (String) configs.get("namespace");
            log.info("Got vault namespace from config : {}", namespace);
        } else {
            throw new VaultInitializationException("namespace is not provided for vault config, please add the namespace configuration");
        }

        vault = new VaultBuilder()
            .uri(uri)
            .namespace(namespace)
            .auth(configuredAuthMechanism(configs))
            .build();
        log.info("VaultSecretProvider initialized with Hashicorp vault at {} for {} namespace.", uri, namespace);
    }

    private VaultAuth configuredAuthMechanism(Map<String, ?> configs) {
        String authMechanism = null;
        if (configs.containsKey("auth.mechanism")) {
            authMechanism = (String) configs.get("auth.mechanism");
        }
        if (authMechanism == null || authMechanism.isBlank()) {
            throw new VaultInitializationException("Vault auth mechanism must be supplied." +
                " Token can be set in worker config against config.providers.vault.param.auth.mechanism");
        }

        switch (authMechanism) {
            case "TOKEN_AUTH":
                return TokenAuth.configuredAuth(configs);
            case "APP_ROLE_AUTH":
                return AppRoleAuth.configuredAuth(configs);
            default:
                throw new VaultInitializationException("Invalid auth.mechanism provided. " +
                    "Valid values are TOKEN_AUTH & APP_ROLE_AUTH");
        }
    }


}