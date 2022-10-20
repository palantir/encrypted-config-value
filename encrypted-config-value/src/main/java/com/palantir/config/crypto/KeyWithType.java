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

import static com.palantir.logsafe.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Throwables;
import com.google.common.io.BaseEncoding;
import com.palantir.config.crypto.algorithm.KeyType;
import org.immutables.value.Value;

/**
 * Stores a key with its type. Supports serializing to and from JSON as a string. The serialized representation is of
 * the form "type:base64-encoded-key".
 */
@Value.Immutable
public abstract class KeyWithType {

    public abstract KeyType getType();

    public abstract Key getKey();

    @Override
    @JsonValue
    public final String toString() {
        return getType() + ":" + BaseEncoding.base64().encode(getKey().bytes());
    }

    @JsonCreator
    public static KeyWithType fromString(String keyWithType) {
        checkArgument(keyWithType.contains(":"), "Key must be in the format <type>:<key in base64>");

        String[] tokens = keyWithType.split(":", 2);
        byte[] decodedKey = BaseEncoding.base64().decode(tokens[1]);

        // legacy RSA key format
        if (tokens[0].equals("RSA")) {
            // try parsing as private key
            RuntimeException parsePrivateKeyException;
            try {
                return KeyType.RSA_PRIVATE.keyFromBytes(decodedKey);
            } catch (RuntimeException e) {
                // ignore; try parsing as public key
                parsePrivateKeyException = e;
            }

            // try parsing as public key
            RuntimeException parsePublicKeyException;
            try {
                return KeyType.RSA_PUBLIC.keyFromBytes(decodedKey);
            } catch (RuntimeException e) {
                // ignore; try parsing as public key
                parsePublicKeyException = e;
            }

            throw new IllegalStateException("unable to parse legacy RSA key.\n"
                    + "Error parsing as private key: " + Throwables.getStackTraceAsString(parsePrivateKeyException)
                    + "\n"
                    + "Error parsing as public key: " + Throwables.getStackTraceAsString(parsePublicKeyException));
        }

        KeyType keyAlg = KeyType.from(tokens[0]);
        return keyAlg.keyFromBytes(decodedKey);
    }
}
