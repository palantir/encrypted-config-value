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

        Namespace namespace = new Namespace(ImmutableMap.of(
                GenerateKeyCommand.ALGORITHM, algorithm,
                GenerateKeyCommand.FILE, tempFilePath.toString()));

        command.run(null, namespace);

        KeyWithAlgorithm keyWithAlgorithm = KeyWithAlgorithm.fromPath(tempFilePath);
        assertThat(keyWithAlgorithm.getAlgorithm(), is(algorithm));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void weDoNotOverwriteAnExistingKeyfile() throws Exception {
        Path tempFilePath = Files.createTempDirectory("temp-key-directory").resolve("test.key");
        String algorithm = "AES";

        // create the file
        Files.createFile(tempFilePath);

        Namespace namespace = new Namespace(ImmutableMap.of(
                GenerateKeyCommand.ALGORITHM, algorithm,
                GenerateKeyCommand.FILE, tempFilePath.toString()));

        command.run(null, namespace);
    }
}
