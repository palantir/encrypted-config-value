/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.palantir.config.crypto.algorithm.Algorithm;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import org.assertj.core.api.HamcrestCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public final class AlgorithmTest {
    private static final String plaintext = "Some top secret plaintext for testing things";

    private final Algorithm algorithm;

    public AlgorithmTest(String _name, Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Test
    public void weGenerateRandomKeys() {
        KeyPair keyPair1 = algorithm.newKeyPair();
        KeyPair keyPair2 = algorithm.newKeyPair();

        assertThat(keyPair1).is(new HamcrestCondition<>(is(not(keyPair2))));
    }

    @Test
    public void weCanEncryptAndDecrypt() throws NoSuchAlgorithmException {
        KeyPair keyPair = algorithm.newKeyPair();

        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        KeyWithType decryptionKey = keyPair.decryptionKey();
        String decrypted = encryptedValue.decrypt(decryptionKey);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    public void theSameStringEncryptsToDifferentCiphertexts() throws NoSuchAlgorithmException {
        KeyPair keyPair = algorithm.newKeyPair();

        EncryptedValue encrypted1 = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);
        EncryptedValue encrypted2 = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        // we don't want to leak that certain values are the same
        assertThat(encrypted1).is(new HamcrestCondition<>(is(not(encrypted2))));
        // paranoia, let's say the equals method is badly behaved
        assertThat(encrypted1.toString()).is(new HamcrestCondition<>(is(not(encrypted2.toString()))));

        // we should naturally decrypt back to the same thing - the plaintext
        KeyWithType decryptionKey = keyPair.decryptionKey();
        String decryptedString1 = encrypted1.decrypt(decryptionKey);
        String decryptedString2 = encrypted2.decrypt(decryptionKey);

        assertThat(decryptedString1).isEqualTo(plaintext);
        assertThat(decryptedString2).isEqualTo(plaintext);
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return ImmutableList.of(
                new Object[] {"AES", Algorithm.AES},
                new Object[] {"RSA", Algorithm.RSA}
                );
    }
}
