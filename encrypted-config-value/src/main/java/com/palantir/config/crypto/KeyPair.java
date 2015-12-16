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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * A public and private key. For symmetric keys, the private key will be absent.
 */
@Value.Immutable
public abstract class KeyPair {
    public static final String KEY_PATH_PROPERTY = "palantir.config.key_path";
    public static final String DEFAULT_PUBLIC_KEY_PATH = "var/conf/encrypted-config-value.key";

    public abstract KeyWithAlgorithm publicKey();

    public abstract Optional<KeyWithAlgorithm> privateKey();

    public final void toFile(Path path) throws IOException {
        publicKey().toFile(path);

        if (privateKey().isPresent()) {
            KeyWithAlgorithm privateKey = privateKey().get();
            Path privatePath = privatePath(path);
            privateKey.toFile(privatePath);
        }
    }

    public static Path privatePath(Path path) {
        Path privatePath = path.resolveSibling(path.getFileName() + ".private");
        return privatePath;
    }

    public static KeyPair of(KeyWithAlgorithm publicKey, KeyWithAlgorithm privateKey) {
        return ImmutableKeyPair.builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
    }

    public static KeyPair symmetric(KeyWithAlgorithm symmetric) {
        return ImmutableKeyPair.builder()
                .publicKey(symmetric)
                .build();
    }

    public static KeyPair fromPath(Path path) throws IOException {
        KeyWithAlgorithm publicKey = KeyWithAlgorithm.fromPath(path);

        Path privatePath = privatePath(path);
        if (!privatePath.toFile().exists()) {
            return KeyPair.symmetric(publicKey);
        }

        KeyWithAlgorithm privateKey = KeyWithAlgorithm.fromPath(privatePath);
        return KeyPair.of(publicKey, privateKey);
    }

    public static KeyPair fromDefaultPath() throws IOException {
        return fromPath(Paths.get(System.getProperty(KEY_PATH_PROPERTY, DEFAULT_PUBLIC_KEY_PATH)));
    }
}
