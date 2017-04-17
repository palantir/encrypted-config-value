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

import com.palantir.config.crypto.algorithm.Algorithm;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public final class GenerateKeyCommand extends Command {

    public static final String FILE = "file";
    public static final String ALGORITHM = "algorithm";

    protected GenerateKeyCommand() {
        super("generate-random-key", "Generates a random key for encrypting config values");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-a", "--algorithm")
            .required(true)
            .type(String.class)
            .dest(ALGORITHM)
            .help("The algorithm to use. Supported values: " + Arrays.toString(Algorithm.values()));

        subparser.addArgument("-f", "--file")
            .required(false)
            .type(String.class)
            .dest(FILE)
            .setDefault(KeyFileUtils.DEFAULT_PUBLIC_KEY_PATH)
            .help("The location to write the key");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String algorithmType = namespace.getString(ALGORITHM);
        String file = namespace.getString(FILE);
        Path path = Paths.get(file);

        Algorithm algorithm = Algorithm.valueOf(algorithmType);
        KeyPair keyPair = algorithm.newKeyPair();
        KeyPairFiles keyPairFiles = KeyFileUtils.keyPairToFile(keyPair, path);

        // print to console, notifying that we did something
        System.out.println("Wrote key to " + path);
        if (!keyPairFiles.pathsEqual()) {
            System.out.println("Wrote private key to " + keyPairFiles.decryptionKeyFile());
        }
    }

}
