/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

public final class GenerateKeyCommandTest {
    private final GenerateKeyCommand command = new GenerateKeyCommand();

    @Test
    public void weGenerateAValidKey() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");
        String algorithm = "AES";
        int keySize = 128;

        Namespace namespace = new Namespace(ImmutableMap.of(
                GenerateKeyCommand.ALGORITHM, algorithm,
                GenerateKeyCommand.KEYSIZE, keySize,
                GenerateKeyCommand.FILE, tempFilePath.toString()));

        command.run(null, namespace);

        KeyWithAlgorithm keyWithAlgorithm = KeyWithAlgorithm.fromPath(tempFilePath);
        assertThat(keyWithAlgorithm.getAlgorithm(), is(algorithm));
        assertThat(keyWithAlgorithm.getKey().length, is(keySize / 8));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void weDoNotOverwriteAnExistingKeyfile() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");
        String algorithm = "AES";
        int keySize = 128;

        // create the file
        Files.createFile(tempFilePath);

        Namespace namespace = new Namespace(ImmutableMap.of(
                GenerateKeyCommand.ALGORITHM, algorithm,
                GenerateKeyCommand.KEYSIZE, keySize,
                GenerateKeyCommand.FILE, tempFilePath.toString()));

        command.run(null, namespace);
    }
}
