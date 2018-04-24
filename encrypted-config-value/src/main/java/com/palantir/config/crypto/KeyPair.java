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

import org.immutables.value.Value;

/**
 * An encryption and decryption key. For symmetric keys, both keys will be the same.
 */
@Value.Immutable
public abstract class KeyPair {

    public abstract KeyWithType encryptionKey();

    public abstract KeyWithType decryptionKey();

    public static KeyPair of(KeyWithType encryptionKey, KeyWithType decryptionKey) {
        return ImmutableKeyPair.builder()
                .encryptionKey(encryptionKey)
                .decryptionKey(decryptionKey)
                .build();
    }

    public static KeyPair symmetric(KeyWithType key) {
        return ImmutableKeyPair.builder()
                .encryptionKey(key)
                .decryptionKey(key)
                .build();
    }
}
