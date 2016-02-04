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

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.palantir.config.crypto.algorithm.AesAlgorithm;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.RsaAlgorithm;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public final class AlgorithmTest {
    private static final String plaintext = "Some top secret plaintext for testing things";

    private final Supplier<Algorithm> algorithmSupplier;

    public AlgorithmTest(String name, Supplier<Algorithm> algorithmSupplier) {
        this.algorithmSupplier = algorithmSupplier;
    }

    @Test
    public void weGenerateRandomKeys() {
        Algorithm algorithm = this.algorithmSupplier.get();

        KeyPair keyPair1 = algorithm.generateKey();
        KeyPair keyPair2 = algorithm.generateKey();

        assertThat(keyPair1, is(not(keyPair2)));
    }

    @Test
    public void weCanEncryptAndDecrypt() throws NoSuchAlgorithmException {
        Algorithm algorithm = this.algorithmSupplier.get();
        KeyPair keyPair = algorithm.generateKey();

        EncryptedValue encryptedValue = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());

        KeyWithAlgorithm decryptionKey = keyPair.privateKey().or(keyPair.publicKey());
        String decrypted = algorithm.getDecryptedString(encryptedValue, decryptionKey);

        assertThat(decrypted, is(plaintext));
    }

    @Test
    public void theSameStringEncryptsToDifferentCiphertexts() throws NoSuchAlgorithmException {
        Algorithm algorithm = this.algorithmSupplier.get();
        KeyPair keyPair = algorithm.generateKey();

        EncryptedValue encrypted1 = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());
        EncryptedValue encrypted2 = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());

        // we don't want to leak that certain values are the same
        assertThat(encrypted1, is(not(encrypted2)));
        // paranoia, let's say the equals method is badly behaved
        assertThat(encrypted1.encryptedValue(), is(not(encrypted2.encryptedValue())));

        // we should naturally decrypt back to the same thing - the plaintext
        KeyWithAlgorithm decryptionKey = keyPair.privateKey().or(keyPair.publicKey());
        String decryptedString1 = algorithm.getDecryptedString(encrypted1, decryptionKey);
        String decryptedString2 = algorithm.getDecryptedString(encrypted2, decryptionKey);

        assertThat(decryptedString1, is(plaintext));
        assertThat(decryptedString2, is(plaintext));
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // TODO: produce this for all implementations of algorithm?

        Supplier<Algorithm> aes = new Supplier<Algorithm>() {
            @Override
            public Algorithm get() {
                return new AesAlgorithm();
            }
        };
        Supplier<Algorithm> rsa = new Supplier<Algorithm>() {
            @Override
            public Algorithm get() {
                return new RsaAlgorithm();
            }
        };

        return ImmutableList.of(
                new Object[] {"AES", aes},
                new Object[] {"RSA", rsa}
                );
    }
}
