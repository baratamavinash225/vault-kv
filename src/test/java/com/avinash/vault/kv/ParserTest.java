package com.avinash.vault.kv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ParserTest {
    @Test
    void parseVaultResponseTest() throws JsonProcessingException {
        String response = "{\"request_id\":\"b39595c5-9107-4066-aef1-303bcc391027\",\"lease_id\":\"\",\"renewable\":false,\"lease_duration\":0,\"data\":{\"data\":{\"registry_pwd\":\"dummyPassword\",\"registry_usr_info\":\"admin\"},\"metadata\":{\"created_time\":\"2022-03-28T10:22:52.997559047Z\",\"deletion_time\":\"\",\"destroyed\":false,\"version\":1}},\"wrap_info\":null,\"warnings\":null,\"auth\":null}";

        ObjectMapper objectMapper = new ObjectMapper();
        VaultResponse vaultResponse = objectMapper.readValue(response, VaultResponse.class);
        log.info(vaultResponse.getData().getData().toString());
    }
}
