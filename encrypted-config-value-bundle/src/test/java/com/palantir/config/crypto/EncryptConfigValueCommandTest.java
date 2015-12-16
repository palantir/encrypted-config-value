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
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class EncryptConfigValueCommandTest {
    private static final String CHARSET = "UTF8";
    private static final String plaintext = "this is a secret message";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final EncryptConfigValueCommand command = new EncryptConfigValueCommand();

    private PrintStream originalSystemOut;

    @Before
    public void setUpStreams() throws UnsupportedEncodingException {
        originalSystemOut = System.out;
        System.setOut(new PrintStream(outContent, false, CHARSET));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(originalSystemOut);
    }

    @Test
    public void weEncryptAndPrintAValue() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");

        KeyWithAlgorithm keyWithAlgorithm = KeyWithAlgorithm.randomKey("AES");
        keyWithAlgorithm.toFile(tempFilePath);

        Namespace namespace = new Namespace(ImmutableMap.of(
                EncryptConfigValueCommand.KEYFILE, tempFilePath.toString(),
                EncryptConfigValueCommand.VALUE, plaintext));

        command.run(null, namespace);

        String output = outContent.toString(CHARSET).trim();

        EncryptedValue configValue = EncryptedValue.of(output);
        String decryptedValue = configValue.getDecryptedValue(keyWithAlgorithm);

        assertThat(decryptedValue, is(plaintext));
    }

    @Test(expected = NoSuchFileException.class)
    public void weFailIfTheKeyfileDoesNotExist() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");

        Namespace namespace = new Namespace(ImmutableMap.of(
                EncryptConfigValueCommand.KEYFILE, tempFilePath.toString(),
                EncryptConfigValueCommand.VALUE, plaintext));

        command.run(null, namespace);
    }
}
