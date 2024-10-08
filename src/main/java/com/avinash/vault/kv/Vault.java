package com.avinash.vault.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.avinash.vault.kv.auth.VaultAuth;
import com.avinash.vault.kv.exceptions.SecretNotFoundException;
import com.avinash.vault.kv.exceptions.VaultException;
import com.avinash.vault.kv.http.JsonBodyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.avinash.vault.kv.http.RetryableHttp.httpClient;

@Builder
@AllArgsConstructor
public class Vault implements SecretProvider {

    private final URI uri;
    private final VaultAuth auth;
    private final String namespace;

    @Override
    public Map<String, String> getSecret(String path) {
        URI fullUri = fullUri(path);
        VaultResponse response = getFromVault(fullUri);
        return response.getData().getData();
    }

    @Override
    public Map<String, String> getSecret(String path, String key) {
        URI fullUri = fullUri(path);
        VaultResponse response = getFromVault(fullUri);
        Map<String, String> map = response.getData().getData();
        if (map.containsKey(key)) {
            return new HashMap<>() {{
                put(key, map.get(key));
            }};
        }
        throw new SecretNotFoundException("Secret value for key " + key + " is not present at path " + path);
    }

    @Override
    public void addSecret(String secretEngine,String secretName) {
        String relativePath = "/v1/"+secretEngine+"/data/"+secretName;
        URI fullUri = fullUri(relativePath);
        HttpRequest request = HttpRequest.newBuilder(fullUri)
                .header("X-Vault-Token", auth.getToken())
                .header("X-Vault-Namespace", namespace)
                .PUT(HttpRequest.BodyPublishers.ofString("{\"data\":{},\"options\":{}}"))
                .timeout(Duration.ofSeconds(10))
                .build();
        try {
            var response = httpClient().send(request, HttpResponse.BodyHandlers.discarding());
            var responseCode = response.statusCode();
            if(200!=responseCode) {
                throw new VaultException("Unable to add secret to vault, got response code : "+responseCode);
            }
        } catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to get a response from vault at " + uri);
        }
    }

    @Override
    public void addSecret(String secretEngine,String secretName,Map<String,String> data,Map<String,String> options) {
        String relativePath = "/v1/"+secretEngine+"/data/"+secretName;
        URI fullUri = fullUri(relativePath);
        VaultRequest vaultRequest = new VaultRequest();
        vaultRequest.setData(data);
        vaultRequest.setOptions(options);
        try {
            ObjectMapper om = new ObjectMapper();
            String payload = om.writeValueAsString(vaultRequest);
            HttpRequest request = HttpRequest.newBuilder(fullUri)
                    .header("X-Vault-Token", auth.getToken())
                    .header("X-Vault-Namespace", namespace)
                    .PUT(HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            var response = httpClient().send(request, HttpResponse.BodyHandlers.discarding());
            var responseCode = response.statusCode();
            if(200!=responseCode) {
                throw new VaultException("Unable to add secret to vault, got response code : "+responseCode);
            }
        } catch(JsonProcessingException p){
            throw new RuntimeException(p);
        }catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to get a response from vault at " + uri);
        }
    }

    @Override
    public void patchSecret(String secretEngine, String secretName, Map<String, String> data, Map<String, String> options) {
        String relativePath = "/v1/"+secretEngine+"/data/"+secretName;
        URI fullUri = fullUri(relativePath);
        VaultRequest vaultRequest = new VaultRequest();
        vaultRequest.setData(data);
        vaultRequest.setOptions(options);
        try {
            ObjectMapper om = new ObjectMapper();
            String payload = om.writeValueAsString(vaultRequest);
            HttpRequest request = HttpRequest.newBuilder(fullUri)
                    .header("Content-Type", "application/merge-patch+json")
                    .header("X-Vault-Token", auth.getToken())
                    .header("X-Vault-Namespace", namespace)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            var response = httpClient().send(request, HttpResponse.BodyHandlers.discarding());
            var responseCode = response.statusCode();
            if(200!=responseCode) {
                throw new VaultException("Unable to add secret to vault, got response code : "+responseCode);
            }
        } catch(JsonProcessingException p){
            throw new RuntimeException(p);
        }catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to get a response from vault at " + uri);
        }
    }

    @Override
    public void destroySecret(String secretEngine, String secretName, Optional<String> secretVersion) {
        String relativePath = "/v1/"+secretEngine+"/destroy/"+secretName;
        URI fullUri = fullUri(relativePath);
        HttpRequest request = HttpRequest.newBuilder(fullUri)
                .header("X-Vault-Token", auth.getToken())
                .header("X-Vault-Namespace", namespace)
                .PUT(HttpRequest.BodyPublishers.ofString("{ \"versions\": ["+secretVersion.orElse("1")+"] }'"))
                .timeout(Duration.ofSeconds(10))
                .build();
        try {
            httpClient().send(request,  HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to get a response from vault at " + uri);
        }
    }


    private VaultResponse getFromVault(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
            .header("X-Vault-Token", auth.getToken())
            .header("X-Vault-Namespace", namespace)
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
        HttpResponse<VaultResponse> response;
        try {
            response = httpClient().withRetries(3).send(request, new JsonBodyHandler<>(VaultResponse.class));
        } catch (Exception e) { //ignore e, as printing it might reveal secrets in logs
            throw new VaultException("Failed to get a response from vault at " + uri);
        }
        return response.body();
    }

    private URI fullUri(String path) {
        try {
            return new URI(uri + path);
        } catch (URISyntaxException e) {
            throw new VaultException("Can not form a proper uri from path : {" + path + "}", e);
        }
    }
}
