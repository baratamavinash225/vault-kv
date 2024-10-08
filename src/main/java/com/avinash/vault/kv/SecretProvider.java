package com.avinash.vault.kv;

import java.util.Map;
import java.util.Optional;

public interface SecretProvider {

    /**
     * @param path the path of the secret in the vault excluding th key
     * @return all the key value pairs in form of a map at the specified path
     */
    Map<String, String> getSecret(String path);

    /**
     * @param path the path of the secret in the vault excluding th key
     * @param key  the specific key to be looked for in the vault on the specified path.
     * @return the key value pair for this key and its secret value in form of a map
     */
    Map<String, String> getSecret(String path, String key);

    /**
     * Creates an empty secret under the Secret Engine
     *
     * @param secretEngine The secret Engine under which the new path (secret) should be created
     * @param secretName Name of the secret
     */
    void addSecret(String secretEngine,String secretName);

    /**
     * Creates a secret under the Secret Engine with provided values
     *
     * @param secretEngine The secret Engine under which the new path (secret) should be created
     * @param secretName Name of the secret
     * @param data Data to add as key value pairs
     * @param options Optional argument key value pairs to influence behaviour
     */
    void addSecret(String secretEngine,String secretName,Map<String,String> data,Map<String,String> options);

    /**
     * Patch Secret
     * - If key value pair is present, it will update the existing values
     * - If key value pair not is present, it will add the values
     * - To delete a value, add null for a key
     *
     * @param secretEngine The secret Engine under which the new path (secret) should be created
     * @param secretName Name of the secret
     * @param data Data to add as key value pairs
     * @param options Optional argument key value pairs to influence behaviour
     */
    void patchSecret(String secretEngine,String secretName,Map<String,String> data,Map<String,String> options);

    /**
     *
     * @param secretEngine Name of the secret engine
     * @param secretName Name of the secret
     * @param secretVersion Version of the secret to destroy
     */
    void destroySecret(String secretEngine, String secretName, Optional<String> secretVersion);
}
