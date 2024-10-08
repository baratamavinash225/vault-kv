package com.avinash.vault.kv.exceptions;

public class VaultException extends RuntimeException {
    public VaultException(String msg) {
        super(msg);
    }

    public VaultException(String msg, Throwable t) {
        super(msg, t);
    }
}