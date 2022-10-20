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

import com.palantir.logsafe.exceptions.SafeRuntimeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class KeyFileUtils {
    public static final String KEY_PATH_PROPERTY = "palantir.config.key_path";
    public static final String DEFAULT_PUBLIC_KEY_PATH = "var/conf/encrypted-config-value.key";

    public static String decryptUsingDefaultKeys(EncryptedValue encryptedValue) {
        KeyPair keyPair;
        try {
            keyPair = keyPairFromDefaultPath();
        } catch (IOException e) {
            throw new SafeRuntimeException("Failed to read key", e);
        }
        return encryptedValue.decrypt(keyPair.decryptionKey());
    }

    public static KeyWithType keyWithTypeFromPath(Path keyPath) throws IOException {
        byte[] contents = Files.readAllBytes(keyPath);
        return KeyWithType.fromString(new String(contents, StandardCharsets.UTF_8));
    }

    public static void keyWithTypeToFile(KeyWithType kwt, Path path) throws IOException {
        byte[] serialized = kwt.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(path, serialized, StandardOpenOption.CREATE_NEW);
    }

    public static KeyPairFiles keyPairToFile(KeyPair keyPair, Path path) throws IOException {
        keyWithTypeToFile(keyPair.encryptionKey(), path);

        Path decryptionKeyPath = path;
        if (!Objects.equals(keyPair.encryptionKey(), keyPair.decryptionKey())) {
            decryptionKeyPath = privatePath(path);
            keyWithTypeToFile(keyPair.decryptionKey(), decryptionKeyPath);
        }
        return ImmutableKeyPairFiles.builder()
                .encryptionKeyFile(path)
                .decryptionKeyFile(decryptionKeyPath)
                .build();
    }

    public static KeyPair keyPairFromPath(Path path) throws IOException {
        KeyWithType encryptionKey = keyWithTypeFromPath(path);

        Path privatePath = privatePath(path);
        if (!privatePath.toFile().exists()) {
            return KeyPair.symmetric(encryptionKey);
        }

        KeyWithType decryptionKey = keyWithTypeFromPath(privatePath);
        return KeyPair.of(encryptionKey, decryptionKey);
    }

    public static KeyPair keyPairFromDefaultPath() throws IOException {
        return keyPairFromPath(Paths.get(System.getProperty(KEY_PATH_PROPERTY, DEFAULT_PUBLIC_KEY_PATH)));
    }

    /**
     * Returns the sibling path of the provided path with ".private" as the extension.
     */
    private static Path privatePath(Path path) {
        Path privatePath = path.resolveSibling(path.getFileName() + ".private");
        return privatePath;
    }

    private KeyFileUtils() {}
}
