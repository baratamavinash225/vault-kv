package com.avinash.vault.kv.http;

public class RetryAttemptExhausted extends RuntimeException {

    public RetryAttemptExhausted(String msg) {
        super(msg);
    }
}
