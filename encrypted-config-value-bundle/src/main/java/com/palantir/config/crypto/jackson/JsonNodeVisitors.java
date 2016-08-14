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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;

public final class JsonNodeVisitors {
    private JsonNodeVisitors() {
        // utilities
    }

    // Check switch at compile time and use throw after switch as default case
    @SuppressWarnings("checkstyle:missingswitchdefault")
    public static <T> T dispatch(ImmutableList<String> location, JsonNode node, JsonNodeVisitor<T> visitor)
                throws JsonProcessingException {
        switch (node.getNodeType()) {
            case ARRAY:
                return visitor.visitArray(location, (ArrayNode) node);
            case BINARY:
                return visitor.visitBinary(location, (BinaryNode) node);
            case BOOLEAN:
                return visitor.visitBoolean(location, (BooleanNode) node);
            case MISSING:
                return visitor.visitMissing(location);
            case NULL:
                return visitor.visitNull(location);
            case NUMBER:
                return visitor.visitNumeric(location, (NumericNode) node);
            case OBJECT:
                return visitor.visitObject(location, (ObjectNode) node);
            case POJO:
                return visitor.visitPojo(location, (POJONode) node);
            case STRING:
                return visitor.visitText(location, (TextNode) node);
        }
        throw new IllegalArgumentException("Unexpected node type " + node.getNodeType());
    }
}
