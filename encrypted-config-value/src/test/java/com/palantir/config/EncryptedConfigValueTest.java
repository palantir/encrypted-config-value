/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Test;

public final class EncryptedConfigValueTest {
    private static final String plaintext = "my secret. I don't want anyone to know this";

    @Test
    public void weCanEncryptAndDecryptAValueUsingBlowfish() {
        KeyWithAlgorithm key = KeyWithAlgorithm.from("Blowfish", "some basic key".getBytes(StandardCharsets.UTF_8));
        String encryptedValue = EncryptedConfigValues.getEncryptedValue(plaintext, key);

        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValue.fromString(encryptedValue);
        String decryptedValue = encryptedConfigValue.getDecryptedValue(key);

        assertThat(decryptedValue, is(plaintext));
    }

    @Test
    public void weCanEncryptAndDecryptAValueUsingAes() throws NoSuchAlgorithmException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("AES", 128);
        String encryptedValue = EncryptedConfigValues.getEncryptedValue(plaintext, key);

        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValue.fromString(encryptedValue);
        String decryptedValue = encryptedConfigValue.getDecryptedValue(key);

        assertThat(decryptedValue, is(plaintext));
    }

    @Test
    public void weCannotDecryptWithTheWrongKey() throws NoSuchAlgorithmException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("Blowfish", 128);
        KeyWithAlgorithm otherKey = KeyWithAlgorithm.randomKey("Blowfish", 128);
        String encryptedValue = EncryptedConfigValues.getEncryptedValue(plaintext, key);

        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValue.fromString(encryptedValue);
        try {
            encryptedConfigValue.getDecryptedValue(otherKey); //throws
            Assert.fail("should have thrown an exception");
        } catch (RuntimeException e) {
            // honk
        }
    }

    @Test(expected = RuntimeException.class)
    public void weCannotEncryptWithAnEmptyKey() {
        KeyWithAlgorithm key = KeyWithAlgorithm.fromString("Blowfish:");
        EncryptedConfigValues.getEncryptedValue(plaintext, key); //throws
    }

    @Test
    public void weCanDeserializeFromAString()
            throws NoSuchAlgorithmException, JsonParseException, JsonMappingException, IOException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("Blowfish", 128);
        String encryptedString = EncryptedConfigValues.getEncryptedValue(plaintext, key);
        String jsonString = "\"" + encryptedString + "\"";

        ObjectMapper mapper = new ObjectMapper();
        EncryptedConfigValue encryptedValue = mapper.readValue(jsonString, EncryptedConfigValue.class);

        String decryptedValue = encryptedValue.getDecryptedValue(key);

        assertThat(decryptedValue, is(plaintext));
    }
}
