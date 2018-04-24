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

import com.palantir.config.crypto.algorithm.Algorithm;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.nio.file.Paths;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public final class EncryptConfigValueCommand extends Command {

    public static final String KEYFILE = "keyfile";
    public static final String VALUE = "value";

    protected EncryptConfigValueCommand() {
        super("encrypt-config-value", "Encrypts a configuration value so it can be stored securely");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-k", "--keyfile")
            .required(false)
            .type(String.class)
            .dest(KEYFILE)
            .setDefault(KeyFileUtils.DEFAULT_PUBLIC_KEY_PATH)
            .help("The location of the (public) key file");

        subparser.addArgument("-v", "--value")
            .required(true)
            .type(String.class)
            .dest(VALUE)
            .help("The value to encrypt");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String keyfile = namespace.getString(KEYFILE);
        String value = namespace.getString(VALUE);

        KeyWithType keyWithType = KeyFileUtils.keyWithTypeFromPath(Paths.get(keyfile));
        Algorithm algorithm = keyWithType.getType().getAlgorithm();

        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyWithType, value);

        // print the resulting encrypted value to the console
        System.out.println(encryptedValue);
    }
}
