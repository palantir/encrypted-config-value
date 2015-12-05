/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

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

    @Before
    public void setUpStreams() throws UnsupportedEncodingException {
        System.setOut(new PrintStream(outContent, false, CHARSET));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void weEncryptAndPrintAValue() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");

        KeyWithAlgorithm keyWithAlgorithm = KeyWithAlgorithm.randomKey("AES", 128);
        keyWithAlgorithm.toFile(tempFilePath);

        Namespace namespace = new Namespace(ImmutableMap.of(
                EncryptConfigValueCommand.KEYFILE, tempFilePath.toString(),
                EncryptConfigValueCommand.VALUE, plaintext));

        command.run(null, namespace);

        String output = outContent.toString(CHARSET).trim();

        EncryptedConfigValue configValue = EncryptedConfigValue.fromString(output);
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
