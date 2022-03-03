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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import com.palantir.config.crypto.util.StringSubstitutionException;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class JsonNodeStringReplacerTest {

    @Mock
    private StrSubstitutor strSubstitutor;
    @Mock
    private Throwable cause;

    private JsonNodeStringReplacer jsonNodeStringReplacer;

    @Before
    public void before() {
        jsonNodeStringReplacer = new JsonNodeStringReplacer(strSubstitutor);
    }

    @Test
    public void visitTextDelegatesToStrSubstitutor() {
        TextNode textNode = new TextNode("abc");
        when(strSubstitutor.replace("abc")).thenReturn("def");
        assertThat(jsonNodeStringReplacer.visitText(textNode)).isEqualTo(new TextNode("def"));
    }

    @Test
    public void visitArrayDispatchesToChildren() {
        ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
        arrayNode.add("abc");
        arrayNode.add(1);

        when(strSubstitutor.replace("abc")).thenReturn("def");

        ArrayNode expected = new ArrayNode(JsonNodeFactory.instance);
        expected.add("def");
        expected.add(1);

        assertThat(jsonNodeStringReplacer.visitArray(arrayNode)).isEqualTo(expected);
    }

    @Test
    public void visitArrayExtendsStringSubstitutionException() {
        ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
        arrayNode.add(1);
        arrayNode.add(2);
        arrayNode.add(new ObjectNode(
                JsonNodeFactory.instance,
                ImmutableMap.<String, JsonNode>of("key", new TextNode("abc"))));
        arrayNode.add(4);

        doThrow(new StringSubstitutionException(cause, "abc")).when(strSubstitutor).replace("abc");

        try {
            jsonNodeStringReplacer.visitArray(arrayNode);
            failBecauseExceptionWasNotThrown(StringSubstitutionException.class);
        } catch (StringSubstitutionException e) {
            assertThat(e.getValue()).isEqualTo("abc");
            assertThat(e.getField()).isEqualTo("[2].key");
        }
    }

    @Test
    public void visitObjectDispatchesToChildren() {
        ObjectNode objectNode = new ObjectNode(
                JsonNodeFactory.instance,
                ImmutableMap.<String, JsonNode>of(
                        "key1", new TextNode("abc"),
                        "key2", new IntNode(1)));

        when(strSubstitutor.replace("abc")).thenReturn("def");

        ObjectNode expected = new ObjectNode(
                JsonNodeFactory.instance,
                ImmutableMap.<String, JsonNode>of(
                        "key1", new TextNode("def"),
                        "key2", new IntNode(1)));

        assertThat(jsonNodeStringReplacer.visitObject(objectNode)).isEqualTo(expected);
    }

    @Test
    public void visitObjectExtendsStringSubstitutionException() {
        ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
        arrayNode.add(1);
        arrayNode.add(2);
        arrayNode.add(new ObjectNode(
                JsonNodeFactory.instance,
                ImmutableMap.<String, JsonNode>of("key", new TextNode("abc"))));
        arrayNode.add(4);

        ObjectNode objectNode = new ObjectNode(
                JsonNodeFactory.instance,
                ImmutableMap.<String, JsonNode>of(
                        "key1", new IntNode(1),
                        "key2", arrayNode));

        doThrow(new StringSubstitutionException(cause, "abc")).when(strSubstitutor).replace("abc");

        try {
            jsonNodeStringReplacer.visitObject(objectNode);
            failBecauseExceptionWasNotThrown(StringSubstitutionException.class);
        } catch (StringSubstitutionException e) {
            assertThat(e.getValue()).isEqualTo("abc");
            assertThat(e.getField()).isEqualTo("key2[2].key");
        }
    }

    @Test
    public void visitBinaryPassesThrough() {
        BinaryNode binaryNode = mock(BinaryNode.class);
        assertThat(jsonNodeStringReplacer.visitBinary(binaryNode)).isEqualTo(binaryNode);
    }

    @Test
    public void visitBooleanPassesThrough() {
        BooleanNode booleanNode = mock(BooleanNode.class);
        assertThat(jsonNodeStringReplacer.visitBoolean(booleanNode)).isEqualTo(booleanNode);
    }

    @Test
    public void visitNumericPassesThrough() {
        NumericNode numericNode = mock(NumericNode.class);
        assertThat(jsonNodeStringReplacer.visitNumeric(numericNode)).isEqualTo(numericNode);
    }

    @Test
    public void visitPojoPassesThrough() {
        POJONode pojoNode = mock(POJONode.class);
        assertThat(jsonNodeStringReplacer.visitPojo(pojoNode)).isEqualTo(pojoNode);
    }

    @Test
    public void visitNullReturnsNullNodeInstance() {
        assertThat(jsonNodeStringReplacer.visitNull()).isEqualTo(NullNode.getInstance());
    }

    @Test
    public void visitMissingReturnsMissingNodeInstance() {
        assertThat(jsonNodeStringReplacer.visitMissing()).isEqualTo(MissingNode.getInstance());
    }
}
