package com.avinash.vault.kv.exceptions;

public class SecretNotFoundException extends VaultException {
    public SecretNotFoundException(String msg) {
        super(msg);
    }
}
