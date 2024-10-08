package com.avinash.vault.kv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultResponse {
    private String request_id;
    private String lease_id;
    private boolean renewable;
    private float lease_duration;
    private InternalData data;
    private String wrap_info = null;
    private String warnings = null;
    private Auth auth = null;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Auth {
        private String client_token;
        private String accessor;
        private List<String> policies = null;
        private List<String> token_policies = null;
        private Metadata metadata;
        private Long lease_duration;
        private Boolean renewable;
        private String entity_id;
        private String token_type;
        private Boolean orphan;
        private Object mfa_requirement;
        private Long num_uses;
    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class InternalData {
    private Map<String, String> data;
    private Metadata metadata;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Metadata {
    private String created_time;
    private String deletion_time;
    private boolean destroyed;
    private float version;
}

