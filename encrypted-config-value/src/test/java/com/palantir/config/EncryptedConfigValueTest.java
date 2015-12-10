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
    private static final KeyWithAlgorithm blowfishKey
            = KeyWithAlgorithm.from("Blowfish", "some basic key".getBytes(StandardCharsets.UTF_8));

    @Test
    public void weCanConstructFromAValidString() {
        String valid = "enc:TCkE/OT7xsKWqP4SRNBEj54Pk7wDMQzMGJtX90toFuGeejM/LQBDTZ8hEaKQt/3i";
        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValue.fromString(valid);
        assertThat(encryptedConfigValue.getDecryptedValue(blowfishKey), is(plaintext));
    }

    @Test(expected = IllegalArgumentException.class)
    public void weFailToConstructWithInvalidPrefix() {
        String invalid = "anc:TCkE/OT7xsKWqP4SRNBEj54Pk7wDMQzMGJtX90toFuGeejM/LQBDTZ8hEaKQt/3i";
        EncryptedConfigValue.fromString(invalid); // throws
    }

    @Test(expected = IllegalArgumentException.class)
    public void weFailToConstructWithAnInvalidEncryptedValue() {
        String invalid = "enc:verysecret";
        EncryptedConfigValue.fromEncryptedString(invalid);
    }


    @Test
    public void weCannotDecryptWithTheWrongKey() throws NoSuchAlgorithmException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("Blowfish", 128);
        KeyWithAlgorithm otherKey = KeyWithAlgorithm.randomKey("Blowfish", 128);
        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValues.getEncryptedConfigValue(plaintext, key);

        try {
            encryptedConfigValue.getDecryptedValue(otherKey); //throws
            Assert.fail("should have thrown an exception");
        } catch (RuntimeException e) {
            // honk
        }
    }

    @Test
    public void weCanDeserializeFromAString()
            throws NoSuchAlgorithmException, JsonParseException, JsonMappingException, IOException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("Blowfish", 128);
        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValues.getEncryptedConfigValue(plaintext, key);

        String jsonString = "\"" + encryptedConfigValue.toString() + "\"";

        ObjectMapper mapper = new ObjectMapper();
        EncryptedConfigValue encryptedValue = mapper.readValue(jsonString, EncryptedConfigValue.class);

        String decryptedValue = encryptedValue.getDecryptedValue(key);

        assertThat(decryptedValue, is(plaintext));
    }
}
