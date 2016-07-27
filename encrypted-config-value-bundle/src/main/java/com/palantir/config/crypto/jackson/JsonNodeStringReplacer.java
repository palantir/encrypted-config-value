/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.text.StrSubstitutor;

public final class JsonNodeStringReplacer implements JsonNodeVisitor<JsonNode> {

    private final StrSubstitutor substitutor;

    public JsonNodeStringReplacer(StrSubstitutor substitutor) {
        this.substitutor = substitutor;
    }

    @Override
    public JsonNode visitArray(ImmutableList<String> location, ArrayNode arrayNode) throws JsonProcessingException {
        ArrayNode newArrayNode = arrayNode.arrayNode();

        int index = 0;
        for (JsonNode node : arrayNode) {
            newArrayNode.add(JsonNodeVisitors.dispatch(with(location, "[" + index + "]"), node, this));
            index++;
        }
        return newArrayNode;
    }

    @Override
    public JsonNode visitBinary(ImmutableList<String> location, BinaryNode binaryNode) {
        return binaryNode;
    }

    @Override
    public JsonNode visitBoolean(ImmutableList<String> location, BooleanNode booleanNode) {
        return booleanNode;
    }

    @Override
    public JsonNode visitNull(ImmutableList<String> location) {
        return NullNode.getInstance();
    }

    @Override
    public JsonNode visitMissing(ImmutableList<String> location) {
        return MissingNode.getInstance();
    }

    @Override
    public JsonNode visitNumeric(ImmutableList<String> location, NumericNode numericNode) {
        return numericNode;
    }

    @Override
    public JsonNode visitObject(ImmutableList<String> location, ObjectNode objectNode) throws JsonProcessingException {
        ObjectNode newObjectNode = objectNode.objectNode();
        Iterator<Map.Entry<String, JsonNode>> entryIterator = objectNode.fields();
        while (entryIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = entryIterator.next();
            String field = entry.getKey();
            JsonNode node = entry.getValue();
            newObjectNode.set(field, JsonNodeVisitors.dispatch(with(location, field), node, this));
        }
        return newObjectNode;
    }

    @Override
    public JsonNode visitPojo(ImmutableList<String> location, POJONode pojoNode) {
        // Anything to do here?
        return pojoNode;
    }

    @Override
    public JsonNode visitText(ImmutableList<String> location, TextNode textNode) throws JsonProcessingException {
        try {
            return TextNode.valueOf(substitutor.replace(textNode.textValue()));
        } catch (RuntimeException e) {
            String key = Joiner.on(".").join(location);
            throw new JsonParseException(String.format(
                    "The encrypted value for key '%s' could not be decrypted - maybe the encryption key is invalid",
                    key),
                    JsonLocation.NA);
        }
    }

    private static ImmutableList<String> with(ImmutableList<String> list, String element) {
        return ImmutableList.<String>builder().addAll(list).add(element).build();
    }
}
