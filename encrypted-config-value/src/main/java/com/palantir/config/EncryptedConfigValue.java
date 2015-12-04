/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

@Value.Immutable
public interface EncryptedConfigValue {

    @JsonValue
    String encryptedValue();

    default String getDecryptedValue(KeyWithAlgorithm key) {
        return EncryptedConfigValues.getDecryptedValue(encryptedValue(), key);
    }

    static EncryptedConfigValue fromString(String encryptedValue) {
        return ImmutableEncryptedConfigValue.builder().encryptedValue(encryptedValue).build();
    }
}
