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
import com.palantir.config.crypto.util.StringSubstitutionException;
import java.util.Iterator;
import java.util.Map;

public final class JsonNodeStringReplacer implements JsonNodeVisitor<JsonNode> {

    private final Substitutor substitutor;

    public JsonNodeStringReplacer(Substitutor substitutor) {
        this.substitutor = substitutor;
    }

    @Override
    public JsonNode visitArray(ArrayNode arrayNode) {
        ArrayNode newArrayNode = arrayNode.arrayNode();
        int index = 0;
        for (JsonNode node : arrayNode) {
            try {
                newArrayNode.add(JsonNodeVisitors.dispatch(node, this));
            } catch (StringSubstitutionException e) {
                throw e.extend(index);
            }
            index++;
        }
        return newArrayNode;
    }

    @Override
    public JsonNode visitBinary(BinaryNode binaryNode) {
        return binaryNode;
    }

    @Override
    public JsonNode visitBoolean(BooleanNode booleanNode) {
        return booleanNode;
    }

    @Override
    public JsonNode visitNull() {
        return NullNode.getInstance();
    }

    @Override
    public JsonNode visitMissing() {
        return MissingNode.getInstance();
    }

    @Override
    public JsonNode visitNumeric(NumericNode numericNode) {
        return numericNode;
    }

    @Override
    public JsonNode visitObject(ObjectNode objectNode) {
        ObjectNode newObjectNode = objectNode.objectNode();
        Iterator<Map.Entry<String, JsonNode>> entryIterator = objectNode.fields();
        while (entryIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = entryIterator.next();
            String field = entry.getKey();
            JsonNode node = entry.getValue();
            try {
                newObjectNode.set(field, JsonNodeVisitors.dispatch(node, this));
            } catch (StringSubstitutionException e) {
                throw e.extend(field);
            }
        }
        return newObjectNode;
    }

    @Override
    public JsonNode visitPojo(POJONode pojoNode) {
        // Anything to do here?
        return pojoNode;
    }

    @Override
    public JsonNode visitText(TextNode textNode) {
        return TextNode.valueOf(substitutor.replace(textNode.textValue()));
    }
}
