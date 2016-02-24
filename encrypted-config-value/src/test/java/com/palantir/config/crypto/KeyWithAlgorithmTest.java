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

package com.palantir.config.crypto;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;

public final class KeyWithAlgorithmTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String kwaString = "AES:rqrvWpLld+wKLOyxJYxQVg==";
    private static final KeyWithAlgorithm kwa = KeyWithAlgorithm.fromString(kwaString);

    @Test
    public void testSerialization()
            throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        String serialized = mapper.writeValueAsString(kwa);

        String expectedSerialization = String.format("\"%s\"", kwaString);
        assertThat(serialized, is(expectedSerialization));

        KeyWithAlgorithm deserialized = mapper.readValue(serialized, KeyWithAlgorithm.class);
        assertThat(deserialized, is(kwa));
    }
}
