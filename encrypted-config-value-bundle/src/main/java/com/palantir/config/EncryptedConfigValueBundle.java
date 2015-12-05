/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public final class EncryptedConfigValueBundle implements Bundle {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addCommand(new GenerateKeyCommand());
        bootstrap.addCommand(new EncryptConfigValueCommand());
    }

    @Override
    public void run(Environment environment) {}

}
