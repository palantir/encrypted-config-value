/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public final class EncryptedConfigModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public EncryptedConfigModule() {
        addDeserializer(String.class, new EncryptedStringDeserializer());
    }
}
