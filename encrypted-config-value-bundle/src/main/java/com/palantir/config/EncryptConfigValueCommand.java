/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.nio.file.Paths;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public final class EncryptConfigValueCommand extends Command {

    public static final String KEYFILE = "keyfile";
    public static final String VALUE = "value";

    protected EncryptConfigValueCommand() {
        super("encrypt-config-value", "Encrypts a config value so that it can be stored securely");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-k", "--keyfile")
            .required(false)
            .type(String.class)
            .dest(KEYFILE)
            .setDefault(GenerateKeyCommand.DEFAULT_KEY_FILE_LOCATION)
            .help("The location of the key file");

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

        KeyWithAlgorithm keyWithAlgorithm = KeyWithAlgorithm.fromPath(Paths.get(keyfile));
        EncryptedConfigValue ecv = EncryptedConfigValues.getEncryptedConfigValue(value, keyWithAlgorithm);

        System.out.println(ecv);
    }
}
