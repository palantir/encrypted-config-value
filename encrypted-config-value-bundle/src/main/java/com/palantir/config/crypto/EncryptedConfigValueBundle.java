/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto;

import io.dropwizard.Bundle;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public final class EncryptedConfigValueBundle implements Bundle {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addCommand(new GenerateKeyCommand());
        bootstrap.addCommand(new EncryptConfigValueCommand());
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                            new DecryptingVariableSubstitutor()));
    }

    @Override
    public void run(Environment environment) {}

}
