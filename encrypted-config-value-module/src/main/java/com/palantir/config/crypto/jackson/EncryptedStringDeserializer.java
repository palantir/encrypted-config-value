/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.palantir.config.crypto.DecryptingVariableSubstitutor;
import java.io.IOException;
import org.apache.commons.lang3.text.StrSubstitutor;

public final class EncryptedStringDeserializer extends JsonDeserializer<String> {

    private final StrSubstitutor substitutor;

    public EncryptedStringDeserializer() {
        this.substitutor = new DecryptingVariableSubstitutor();
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return substitutor.replace(parser.getValueAsString());
    }
}
