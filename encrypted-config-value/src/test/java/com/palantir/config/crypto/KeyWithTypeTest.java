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

package com.palantir.config.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public final class KeyWithTypeTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String kwtString = "AES:rqrvWpLld+wKLOyxJYxQVg==";
    private static final KeyWithType kwt = KeyWithType.fromString(kwtString);

    @Test
    public void testSerialization() throws IOException {
        String serialized = mapper.writeValueAsString(kwt);

        String expectedSerialization = String.format("\"%s\"", kwtString);
        assertThat(serialized).isEqualTo(expectedSerialization);

        KeyWithType deserialized = mapper.readValue(serialized, KeyWithType.class);
        assertThat(deserialized.toString()).isEqualTo(kwt.toString());
    }
}
