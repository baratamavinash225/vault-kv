package com.avinash.vault.kv.http;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

@Slf4j
public class RetryableHttp {

    private int maxRetries = 3;

    private static final HttpClient HTTP_CLIENT = HttpClient
        .newBuilder()
        .executor(Executors.newSingleThreadExecutor(r -> new Thread(r, "secret-fetcher")))
        .build();

    public static RetryableHttp httpClient() {
        return new RetryableHttp();
    }

    public RetryableHttp withRetries(int retries) {
        this.maxRetries = retries;
        return this;
    }

    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        return send(request, responseBodyHandler, 3);
    }

    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, int retries) throws IOException, InterruptedException {
        try {
            return HTTP_CLIENT.send(request, responseBodyHandler);
        }
        // Connection rest by peer results in an IOException, which we are retrying
        //TODO: We should also try 50x response codes
        catch (IOException e) {
            if (retries == 0) throw new RetryAttemptExhausted("All retries exceeded for the Http request: " + request);
            sleep(backOff(--retries));
            return send(request, responseBodyHandler, retries);
        }
    }

    private int backOff(int attemptsLeft) {
        return (int) (5 + Math.pow(2, maxRetries - attemptsLeft - 1));
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            //ignore
        }
    }
}
