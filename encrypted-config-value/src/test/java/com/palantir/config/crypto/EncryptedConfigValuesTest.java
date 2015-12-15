/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public final class EncryptedConfigValuesTest {
    private static final String plaintext = "my secret. I don't want anyone to know this";
    private static final KeyWithAlgorithm blowfishKey
            = KeyWithAlgorithm.from("Blowfish", "some basic key".getBytes(StandardCharsets.UTF_8));

    @Test
    public void weCanEncryptAndDecryptAValueUsingBlowfish() {
        EncryptedConfigValue encryptedConfigValue
                = EncryptedConfigValues.getEncryptedConfigValue(plaintext, blowfishKey);

        String decryptedValue = encryptedConfigValue.getDecryptedValue(blowfishKey);

        assertThat(decryptedValue, is(plaintext));
        System.out.println(encryptedConfigValue);
    }

    @Test
    public void weCanEncryptAndDecryptAValueUsingAes() throws NoSuchAlgorithmException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("AES", 128);
        EncryptedConfigValue encryptedConfigValue = EncryptedConfigValues.getEncryptedConfigValue(plaintext, key);

        String decryptedValue = encryptedConfigValue.getDecryptedValue(key);

        assertThat(decryptedValue, is(plaintext));
    }

    @Test(expected = RuntimeException.class)
    public void weCannotEncryptWithAnEmptyKey() {
        KeyWithAlgorithm key = KeyWithAlgorithm.fromString("Blowfish:");
        EncryptedConfigValues.getEncryptedConfigValue(plaintext, key); //throws
    }
}
