package com.avinash.vault.kv.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.avinash.vault.kv.exceptions.VaultException;
import com.avinash.vault.kv.exceptions.VaultInitializationException;
import com.avinash.vault.kv.VaultResponse;
import com.avinash.vault.kv.VaultResponse.Auth;
import com.avinash.vault.kv.http.JsonBodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.avinash.vault.kv.http.RetryableHttp.httpClient;

@Slf4j
public class AppRoleAuth implements VaultAuth {
    public static final Long TOKEN_REFRESH_THRESHOLD = 3600L; // 1 hour
    private final String namespace;
    private final String roleId;
    private final String secretId;
    private final URI vaultUri;
    private String cachedToken;
    private Long expiryAt;

    public AppRoleAuth(String roleId, String secretId) {
        //Defaulted to non-prod namespace
        this("<>", roleId, secretId);
    }

    public AppRoleAuth(String namespace, String roleId, String secretId) {
        this("<>", namespace, roleId, secretId);
    }

    public AppRoleAuth(URI uri, String namespace, String roleId, String secretId) {
        this.vaultUri = uri;
        this.namespace = namespace;
        this.roleId = roleId;
        this.secretId = secretId;
    }

    public AppRoleAuth(String uri, String namespace, String roleId, String secretId) {
        this.vaultUri = URI.create(uri);
        this.namespace = namespace;
        this.roleId = roleId;
        this.secretId = secretId;
    }

    public static VaultAuth configuredAuth(Map<String, ?> configs) {
        URI uri = URI.create("<>");
        String namespace;
        String roleId = null;
        String secretId = null;

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
            throw new VaultInitializationException("Namespace is must to be supplied with the parameter namespace for " +
                    "worker configurations to start with");
        }

        if (configs.containsKey("role.id")) {
            //roleId = EncryptionUtil.decrypt((String) configs.get("role.id")); //TODO This has to be in working state
            roleId = (String) configs.get("role.id");
            log.info("Got app role id from config : ******");
        }

        if (roleId == null || roleId.isBlank()) {
            throw new VaultInitializationException("Vault role id must be supplied with auth.mechanism APP_ROLE_AUTH." +
                    " role.id can be set in worker config against config.providers.vault.param.role.id");
        }

        if (configs.containsKey("secret.id")) {
            secretId = (String) configs.get("secret.id");
            log.info("Got app secret id from config : **********");
        }

        if (secretId == null || secretId.isBlank()) {
            throw new VaultInitializationException("Vault secret id must be supplied with auth.mechanism APP_ROLE_AUTH." +
                    " secret.id can be set in worker config against config.providers.vault.param.secret.id");
        }

        return new AppRoleAuth(uri, namespace, roleId, secretId);
    }

    @Override
    public String getToken() {
        if (cachedToken == null || tokenIsExpired()) {
            refreshToken();
        }
        return cachedToken;
    }

    private boolean tokenIsExpired() {
        return expiryAt < Instant.now().getEpochSecond();
    }

    /**
     * We use an app role id and secret id to get a new service token for vault access. This token is valid for
     * 32 days by default. We set the expiry of token to be 1 hour before actual expiry to have enough time to refresh
     * it, before it actually expires during usage once returned.
     */
    @Override
    public void refreshToken() {
        ObjectNode rootNode = new ObjectMapper().createObjectNode();
        rootNode.put("role_id", roleId);
        rootNode.put("secret_id", secretId);

        HttpRequest request = HttpRequest.newBuilder(fullUri("/v1/auth/approle/login"))
                .header("X-Vault-Namespace", namespace)
                .POST(HttpRequest.BodyPublishers.ofString(rootNode.toString()))
                .timeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpResponse<VaultResponse> response = httpClient().withRetries(3).send(request, new JsonBodyHandler<>(VaultResponse.class));

            Auth auth = response.body().getAuth();
            expiryAt = Instant.now().getEpochSecond() + auth.getLease_duration() - TOKEN_REFRESH_THRESHOLD;
            cachedToken = auth.getClient_token();
        } catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to refresh token for Hashcorp vault at " + vaultUri);
        }
        log.info("Refreshed vault token successfully!");
    }

    private URI fullUri(String path) {
        try {
            return new URI(vaultUri + path);
        } catch (URISyntaxException e) {
            throw new VaultException("Can not form a proper uri from path : {" + path + "}", e);
        }
    }
}

