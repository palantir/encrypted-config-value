/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import org.immutables.value.Value;

@Value.Immutable
public abstract class EncryptedConfigValue {

    private static final String PREFIX = "enc:";

    abstract String encryptedValue();

    public final String getDecryptedValue(KeyWithAlgorithm key) {
        return EncryptedConfigValues.getDecryptedValue(encryptedValue(), key);
    }

    /**
     * Tries to decrypt using the key at the default path.
     * @see KeyWithAlgorithm#DEFAULT_KEY_PATH
     * @return the decrypted value
     */
    public final String getDecryptedValue() {
        try {
            return getDecryptedValue(KeyWithAlgorithm.fromDefaultPath());
        } catch (IOException e) {
            throw new RuntimeException("Was unable to read key", e);
        }
    }

    @Override
    @JsonValue
    public final String toString() {
        return PREFIX + encryptedValue();
    }

    /**
     * @param encryptedValue - a string of the form "prefix:encrypted-string-in-base-64".
     * @return an EncryptedConfigValue for this encrypted string
     */
    public static EncryptedConfigValue fromString(String encryptedValue) {
        checkArgument(encryptedValue.startsWith(PREFIX),
                "encrypted value must begin with " + PREFIX + " but is " + encryptedValue);
        return fromEncryptedString(encryptedValue.substring(PREFIX.length()));
    }

    /**
     * @param encryptedString - an encrypted string to wrap.
     * @return an EncryptedConfigValue for this encrypted string
     */
    public static EncryptedConfigValue fromEncryptedString(String encryptedString) {
        EncryptedConfigValues.getBytesAndValidate(encryptedString); // validate is base-64

        return ImmutableEncryptedConfigValue.builder()
                .encryptedValue(encryptedString)
                .build();
    }
}
