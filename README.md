## vault-kv

This is implementation of ConfigProvider as part of kafka connect. For more details check [Externalizing Secrets in Kafka Connect](https://docs.confluent.io/platform/current/connect/security.html#externalizing-secrets).
However, its usage will not be limited to kafka connect and can be extended to any other service which wants to make use of key-vault to get secrets from HashiCorp Vault(or any other vault based on HashiCorp).

### Kafka-connect

Configuration required in kafka connect worker config if you specify the mechanism to be AppRole based Authentication :
```
    "config.providers": "vault"
    "config.providers.vault.class": "com.avinash.vault.kv.VaultSecretProvider"
    "config.providers.vault.param.url": "<>"
    "config.providers.vault.param.namespace": "<namespace>"
    "config.providers.vault.param.auth.mechanism": "APP_ROLE_AUTH"
    "config.providers.vault.param.role.id": "<roleId>"
    "config.providers.vault.param.secret.id": "<secretId>"
```

Configuration required in kafka connect worker config if you specify the mechanism to be AppRole based Authentication :
```
    "config.providers": "vault"
    "config.providers.vault.class": "com.avinash.vault.kv.VaultSecretProvider"
    "config.providers.vault.param.url": "<>"
    "config.providers.vault.param.namespace": "<namespace>"
    "config.providers.vault.param.auth.mechanism": "TOKEN_AUTH"
    "config.providers.vault.param.token": "<tokenIngpgEncryptedFormat>"
```

In Connector configuration request to create a connector instance, the secret can be specified as shown below in for key target.cluster.ssl.truststore.password

```
{
  "name": "source-mirror-userinteraction-collstore-record-7",
  "config": {
     .
     .
     .
    "target.cluster.ssl.truststore.password": "${vault:/v1/kv/data/avinash/*:truststore_password}",
  }
}
```

The dynamic reference ${vault:/v1/kv/data/avinash/mm2:truststore_password} is divided in three parts
1. _**vault**_ stands for the secret provider which must match with value of <b>config.providers</b> in kafka connect configuration.
2. **_/v1/kv/data/avinash/** is the path to the secret in Vault, note v1 and data are to be added as shown. The actual path in UI looks like kv/avinash/*
3. **_truststore_password_** is the key in the secret whose value will be referenced at the time of connector creation.

The secret value will not be logged anywhere and remains in memory for usage and then destroyed.

### JVM Applications

Create a vault Object using the below. `vaultSecretMap` in the below code will have the entire keyValue pair. You can get the specific key if you wish to using
`vaultSecretMap.get(<key>)`

```
    val vault = Vault
      .builder()
      .uri(URI.create(<VAULT_URI>))))
      .auth(new AppRoleAuth(<VAULT_URI>))
        , <VAULT_NAMESPACE>))
        , <VAULT_ROLE_ID>))
        , <VAULT_SECRET_ID>))))
      .namespace(<VAULT_NAMESPACE>)))
      .build()

    val vaultSecretMap = vault.getSecret(s"/v1/kv/data/$kvPath")
    vaultSecretMap
```