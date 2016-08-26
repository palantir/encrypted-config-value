/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.config.crypto.DecryptingVariableSubstitutor;
import java.io.File;
import java.io.IOException;

public final class EncryptedConfigMapperUtils {

    private EncryptedConfigMapperUtils() {}

    public static <T> T getConfig(File configFile, Class<T> clazz, ObjectMapper mapper)
            throws JsonParseException, JsonMappingException, IOException {
        JsonNode configNode = mapper.readValue(configFile, JsonNode.class);
        JsonNode substitutedNode = JsonNodeVisitors.dispatch(
                mapper.valueToTree(configNode),
                new JsonNodeStringReplacer(new DecryptingVariableSubstitutor()));
        return mapper.treeToValue(substitutedNode, clazz);
    }

}
