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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;

public interface JsonNodeVisitor<T> {

    T visitArray(ImmutableList<String> location, ArrayNode arrayNode) throws JsonProcessingException;

    T visitBinary(ImmutableList<String> location, BinaryNode binaryNode) throws JsonProcessingException;

    T visitBoolean(ImmutableList<String> location, BooleanNode booleanNode) throws JsonProcessingException;

    T visitNull(ImmutableList<String> location) throws JsonProcessingException;

    T visitMissing(ImmutableList<String> location) throws JsonProcessingException;

    T visitNumeric(ImmutableList<String> location, NumericNode numericNode) throws JsonProcessingException;

    T visitObject(ImmutableList<String> location, ObjectNode objectNode) throws JsonProcessingException;

    T visitPojo(ImmutableList<String> location, POJONode pojoNode) throws JsonProcessingException;

    T visitText(ImmutableList<String> location, TextNode textNode) throws JsonProcessingException;

}
