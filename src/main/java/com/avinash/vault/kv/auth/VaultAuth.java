package com.avinash.vault.kv.auth;

public interface VaultAuth {
    String getToken();

    default void refreshToken() {
    }
}
