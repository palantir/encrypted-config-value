/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto;

import io.dropwizard.configuration.ConfigurationException;
import java.util.Collection;

public class ConfigurationDecryptionException extends ConfigurationException {

    public ConfigurationDecryptionException(String path, Collection<String> errors) {
        super(path, errors);
    }

    private static final long serialVersionUID = 1L;

}
