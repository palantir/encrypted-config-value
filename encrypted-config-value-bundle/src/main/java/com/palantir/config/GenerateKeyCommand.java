/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.nio.file.Paths;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public final class GenerateKeyCommand extends Command {

    public static final String DEFAULT_KEY_FILE_LOCATION = "var/conf/encrypted-config-value.key";
    public static final String KEYSIZE = "keysize";
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
            .help("The algorithm to use (see https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator)");

        subparser.addArgument("-n", "--keysize")
            .required(true)
            .type(Integer.class)
            .dest(KEYSIZE)
            .help("The size of the key in bits");

        subparser.addArgument("-f", "--file")
            .required(false)
            .type(String.class)
            .dest(FILE)
            .setDefault(DEFAULT_KEY_FILE_LOCATION)
            .help("The location to write the key");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String algorithm = namespace.getString(ALGORITHM);
        int keySize = namespace.getInt(KEYSIZE);
        String file = namespace.getString(FILE);

        KeyWithAlgorithm randomKey = KeyWithAlgorithm.randomKey(algorithm, keySize);
        randomKey.toFile(Paths.get(file));
    }

}
