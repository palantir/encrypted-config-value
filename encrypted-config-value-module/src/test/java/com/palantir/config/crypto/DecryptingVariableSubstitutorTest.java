/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.Algorithms;
import com.palantir.config.crypto.util.StringSubstitutionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DecryptingVariableSubstitutorTest {
    private static final Algorithm ALGORITHM = Algorithms.getInstance("RSA");
    private static final KeyPair KEY_PAIR = ALGORITHM.generateKey();
    private static String previousProperty;

    private final DecryptingVariableSubstitutor substitutor = new DecryptingVariableSubstitutor();

    @BeforeClass
    public static void setUp() throws IOException {
        previousProperty = System.getProperty(KeyPair.KEY_PATH_PROPERTY);

        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");
        KEY_PAIR.toFile(tempFilePath);
        System.setProperty(KeyPair.KEY_PATH_PROPERTY, tempFilePath.toAbsolutePath().toString());
    }

    @AfterClass
    public static void tearDown() {
        if (previousProperty != null) {
            System.setProperty(KeyPair.KEY_PATH_PROPERTY, previousProperty);
        }
    }

    @Test
    public final void constantsAreNotModified() {
        assertThat(substitutor.replace("abc"), is("abc"));
    }

    @Test
    public final void invalidVariablesThrowStringSubstitutionException() {
        try {
            substitutor.replace("${abc}");
            fail();
        } catch (StringSubstitutionException e) {
            assertThat(e.getValue(), is("abc"));
            assertThat(e.getField(), is(""));
        }
    }

    @Test
    public final void variableIsDecrypted() throws Exception {
        assertThat(substitutor.replace("${enc:" + encrypt("abc") + "}"), is("abc"));
    }

    private String encrypt(String value) {
        return ALGORITHM.getEncryptedValue(value, KEY_PAIR.publicKey()).encryptedValue();
    }
}
