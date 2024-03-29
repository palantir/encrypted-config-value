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

package com.palantir.config.crypto.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.config.crypto.DecryptingVariableSubstitutor;
import java.io.File;
import java.io.IOException;

public final class EncryptedConfigMapperUtils {
    private static final JsonNodeVisitor<JsonNode> JSON_NODE_STRING_REPLACER =
            new JsonNodeStringReplacer(new DecryptingVariableSubstitutor());

    private EncryptedConfigMapperUtils() {}

    public static <T> T getConfig(File configFile, Class<T> clazz, ObjectMapper mapper) throws IOException {
        return substitute(clazz, mapper, mapper.readValue(configFile, JsonNode.class));
    }

    public static <T> T getConfig(String configFileContent, Class<T> clazz, ObjectMapper mapper) throws IOException {
        return substitute(clazz, mapper, mapper.readValue(configFileContent, JsonNode.class));
    }

    private static <T> T substitute(Class<T> clazz, ObjectMapper mapper, JsonNode configNode)
            throws JsonProcessingException {
        JsonNode substitutedNode = JsonNodeVisitors.dispatch(configNode, JSON_NODE_STRING_REPLACER);
        return mapper.treeToValue(substitutedNode, clazz);
    }
}
