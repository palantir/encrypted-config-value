/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

public final class DecryptingVariableSubstitutor extends StrSubstitutor {

    public DecryptingVariableSubstitutor() {
        super(new DecryptingStringLookup());
    }

    private static final class DecryptingStringLookup extends StrLookup<String> {
        @Override
        public String lookup(String encryptedValue) {
            return EncryptedConfigValue.fromString(encryptedValue).getDecryptedValue();
        }
    }

}
