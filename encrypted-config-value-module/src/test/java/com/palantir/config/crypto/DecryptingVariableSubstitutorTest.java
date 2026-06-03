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
import static org.assertj.core.api.Assertions.fail;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.strings;

import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.util.StringSubstitutionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class DecryptingVariableSubstitutorTest {

    private static final Algorithm ALGORITHM = Algorithm.RSA;
    private static final KeyPair KEY_PAIR = ALGORITHM.newKeyPair();
    public static final String TEST_KEY_PATH = DecryptingVariableSubstitutorTest.class.getName() + "test-key";
    private static String previousProperty;

    private final DecryptingVariableSubstitutor substitutor = new DecryptingVariableSubstitutor();

    @BeforeAll
    public static void beforeClass() throws IOException {
        previousProperty = System.getProperty(KeyFileUtils.KEY_PATH_PROPERTY);
        ensureTestKeysExist();
    }

    @AfterAll
    public static void afterClass() {
        if (previousProperty != null) {
            System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, previousProperty);
        }
        System.clearProperty(TEST_KEY_PATH);
    }

    @Test
    public void constantsAreNotModified() {
        assertThat(substitutor.replace("abc")).isEqualTo("abc");
    }

    @Test
    public void invalidEncryptedVariablesThrowStringSubstitutionException() {
        try {
            substitutor.replace("${enc:invalid-contents}");
            fail("fail");
        } catch (StringSubstitutionException e) {
            assertThat(e.getValue()).isEqualTo("enc:invalid-contents");
            assertThat(e.getField()).isEmpty();
        }
    }

    @Test
    public void nonEncryptedVariablesAreNotModified() {
        assertThat(substitutor.replace("${abc}")).isEqualTo("${abc}");
    }

    @Test
    public void variableIsDecrypted() {
        assertThat(substitutor.replace("${" + encrypt("abc") + "}")).isEqualTo("abc");
    }

    @Test
    public void variableIsDecryptedWithRegex() {
        assertThat(substitutor.replace("${" + encrypt("$5") + "}")).isEqualTo("$5");
    }

    @Test
    public void decryptsMultiple() {
        String abc = "${" + encrypt("abc") + "}";
        String def = "${" + encrypt("def") + "}";
        String hello = "${" + encrypt("enc:hello") + "}";
        String source = abc + ":" + def + '.' + hello;
        assertThat(substitutor.replace(source)).isEqualTo("abc:def.enc:hello");
    }

    @Test
    public void decryptsWithPlaceholders() {
        String abc = "${" + encrypt("abc") + "}";
        String def = "${" + encrypt("${enc:test}") + "}";
        String source = abc + ":" + def;
        assertThat(substitutor.replace(source)).isEqualTo("abc:${enc:test}");
    }

    @Test
    void propertyTestValues() throws IOException {
        ensureTestKeysExist();
        qt().withExamples(10_000)
                .forAll(strings().betweenCodePoints(0, 1024).ofLengthBetween(0, 100))
                // RSA test key can only encrypt 190 bytes
                .assuming(plaintext -> plaintext.getBytes(StandardCharsets.UTF_8).length <= 190)
                .checkAssert(plaintext -> {
                    assertThat(substitutor.replace("${" + encrypt(plaintext) + "}"))
                            .isEqualTo(plaintext);
                });
    }

    private static void ensureTestKeysExist() throws IOException {
        String testKeyPath = System.getProperty(TEST_KEY_PATH);
        if (testKeyPath != null && Files.isRegularFile(Path.of(testKeyPath))) {
            return;
        }
        Path tempFilePath = Files.createTempDirectory("temp-key-directory")
                .resolve(ALGORITHM.name() + "-test.key")
                .toAbsolutePath();
        KeyFileUtils.keyPairToFile(KEY_PAIR, tempFilePath);
        String path = tempFilePath.toString();
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, path);
        System.setProperty(TEST_KEY_PATH, path);
    }

    private String encrypt(String value) {
        return ALGORITHM.newEncrypter().encrypt(KEY_PAIR.encryptionKey(), value).toString();
    }
}
