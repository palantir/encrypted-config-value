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
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;

public final class JsonNodeVisitors {
    private JsonNodeVisitors() {
        // utilities
    }

    public static <T> T dispatch(JsonNode node, JsonNodeVisitor<T> visitor) {
        return switch (node.getNodeType()) {
            case ARRAY -> visitor.visitArray((ArrayNode) node);
            case BINARY -> visitor.visitBinary((BinaryNode) node);
            case BOOLEAN -> visitor.visitBoolean((BooleanNode) node);
            case MISSING -> visitor.visitMissing();
            case NULL -> visitor.visitNull();
            case NUMBER -> visitor.visitNumeric((NumericNode) node);
            case OBJECT -> visitor.visitObject((ObjectNode) node);
            case POJO -> visitor.visitPojo((POJONode) node);
            case STRING -> visitor.visitText((TextNode) node);
        };
    }
}
