/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.config.crypto;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.palantir.config.crypto.algorithm.AesAlgorithm;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public final class AesAlgorithmTest {
    private static final String plaintext = "Some top secret plaintext for testing AES";

    @Test
    public void weGenerateRandomKeys() {
        AesAlgorithm aesAlgorithm = new AesAlgorithm();

        KeyWithAlgorithm key1 = aesAlgorithm.generateKey();
        KeyWithAlgorithm key2 = aesAlgorithm.generateKey();

        assertThat(key1, is(not(key2)));
    }

    @Test
    public void weCanEncryptAndDecrypt() throws NoSuchAlgorithmException {
        KeyWithAlgorithm kwa = new AesAlgorithm().generateKey();
        AesAlgorithm aesAlgorithm = new AesAlgorithm();

        EncryptedValue encrypted = aesAlgorithm.getEncryptedValue(plaintext, kwa);
        String decrypted = aesAlgorithm.getDecryptedString(encrypted, kwa);

        assertThat(decrypted, is(plaintext));
    }

    @Test
    public void theSameStringEncryptsToDifferentCiphertexts() throws NoSuchAlgorithmException {
        KeyWithAlgorithm kwa = new AesAlgorithm().generateKey();
        AesAlgorithm aesAlgorithm = new AesAlgorithm();

        EncryptedValue encrypted1 = aesAlgorithm.getEncryptedValue(plaintext, kwa);
        EncryptedValue encrypted2 = aesAlgorithm.getEncryptedValue(plaintext, kwa);

        // we don't want to leak that certain values are the same
        assertThat(encrypted1, is(not(encrypted2)));
        // paranoia, let's say the equals method is badly behaved
        assertThat(encrypted1.encryptedValue(), is(not(encrypted2.encryptedValue())));

        // we should naturally decrypt back to the same thing
        String decryptedString1 = aesAlgorithm.getDecryptedString(encrypted1, kwa);
        String decryptedString2 = aesAlgorithm.getDecryptedString(encrypted2, kwa);

        assertThat(decryptedString1, is(plaintext));
        assertThat(decryptedString2, is(plaintext));
    }
}
