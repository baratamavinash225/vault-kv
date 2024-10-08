package com.avinash.vault.kv;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultRequest {
    private Map<String, String> data;
    private Map<String, String> options;
}