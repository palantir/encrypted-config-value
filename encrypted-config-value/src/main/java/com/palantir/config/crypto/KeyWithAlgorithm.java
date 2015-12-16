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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import org.immutables.value.Value;

@Value.Immutable
public abstract class KeyWithAlgorithm {

    public abstract String getAlgorithm();

    public abstract byte[] getKey();

    @Override
    public final String toString() {
        byte[] encodedKey = Base64.getEncoder().encode(getKey());
        String encodedKeyString = new String(encodedKey, StandardCharsets.UTF_8);
        return getAlgorithm() + ":" + encodedKeyString;
    }

    public final void toFile(Path path) throws IOException {
        byte[] serialized = toString().getBytes(StandardCharsets.UTF_8);
        Files.write(path, serialized, StandardOpenOption.CREATE_NEW);
    }

    public static KeyWithAlgorithm from(String algorithm, byte[] key) {
        return ImmutableKeyWithAlgorithm.builder()
                .algorithm(algorithm)
                .key(key)
                .build();
    }

    public static KeyWithAlgorithm fromString(String keyWithAlgorithm) {
        checkArgument(keyWithAlgorithm.contains(":"), "Key must be in the format <algorithm>:<key in base64>");

        String[] tokens = keyWithAlgorithm.split(":", 2);
        Base64Utils.checkIsBase64(tokens[1]);

        byte[] decodedKey = Base64.getDecoder().decode(tokens[1].getBytes(StandardCharsets.UTF_8));
        return KeyWithAlgorithm.from(tokens[0], decodedKey);
    }

    public static KeyWithAlgorithm fromPath(Path keyPath) throws IOException {
        byte[] contents = Files.readAllBytes(keyPath);
        return KeyWithAlgorithm.fromString(new String(contents, StandardCharsets.UTF_8));
    }
}
