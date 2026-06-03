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
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

import com.palantir.config.crypto.algorithm.Algorithm;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class AlgorithmTest {
    static Stream<Arguments> args() {
        return Stream.of(Algorithm.values())
                .flatMap(algorithm -> Stream.of(
                        Arguments.of(algorithm, "Some top secret plaintext for testing things"),
                        Arguments.of(algorithm, "test")));
    }

    @ParameterizedTest
    @MethodSource("args")
    public void weGenerateRandomKeys(Algorithm algorithm) {
        KeyPair keyPair1 = algorithm.newKeyPair();
        KeyPair keyPair2 = algorithm.newKeyPair();

        assertThat(keyPair1).isNotSameAs(keyPair2).isNotEqualTo(keyPair2);
    }

    @ParameterizedTest
    @MethodSource("args")
    public void weCanEncryptAndDecrypt(Algorithm algorithm, String plaintext) {
        encryptAndDecrypt(algorithm, plaintext);
    }

    @ParameterizedTest
    @MethodSource("args")
    public void theSameStringEncryptsToDifferentCiphertexts(Algorithm algorithm, String plaintext) {
        KeyPair keyPair = algorithm.newKeyPair();

        EncryptedValue encrypted1 = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);
        EncryptedValue encrypted2 = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        // we don't want to leak that certain values are the same
        assertThat(encrypted1).isNotSameAs(encrypted2).isNotEqualTo(encrypted2);
        // paranoia, let's say the equals method is badly behaved
        assertThat(encrypted1.toString()).isNotSameAs(encrypted2.toString()).isNotEqualTo(encrypted2.toString());

        // we should naturally decrypt back to the same thing - the plaintext
        KeyWithType decryptionKey = keyPair.decryptionKey();
        String decryptedString1 = encrypted1.decrypt(decryptionKey);
        String decryptedString2 = encrypted2.decrypt(decryptionKey);

        assertThat(decryptedString1).isEqualTo(plaintext);
        assertThat(decryptedString2).isEqualTo(plaintext);
    }

    @Test
    void aes() {
        qt().withExamples(10_000)
                .forAll(strings().betweenCodePoints(0, 0xD7FF).ofLengthBetween(0, 100_000))
                .checkAssert(plaintext -> encryptAndDecrypt(Algorithm.AES, plaintext));
    }

    @Test
    void rsa() {
        qt().withExamples(100)
                .forAll(strings().betweenCodePoints(0, 0xD7FF).ofLengthBetween(0, 64))
                // RSA test key can only encrypt 190 bytes
                .assuming(plaintext -> plaintext.getBytes(StandardCharsets.UTF_8).length <= 190)
                .checkAssert(plaintext -> encryptAndDecrypt(Algorithm.RSA, plaintext));
    }

    private static void encryptAndDecrypt(Algorithm algorithm, String plaintext) {
        KeyPair keyPair = algorithm.newKeyPair();

        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        KeyWithType decryptionKey = keyPair.decryptionKey();
        String decrypted = encryptedValue.decrypt(decryptionKey);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}
