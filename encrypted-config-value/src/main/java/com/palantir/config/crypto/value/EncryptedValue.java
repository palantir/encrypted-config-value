/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.config.crypto.value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.io.BaseEncoding;
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.KeyWithAlgorithm;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.Algorithms;
import java.io.IOException;

@JsonSubTypes({
        @JsonSubTypes.Type(value = AesEncryptedValue.class),
        @JsonSubTypes.Type(value = RsaEncryptedValue.class)
        })
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
public abstract class EncryptedValue {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PREFIX = "enc:";

    public abstract <T> T accept(EncryptedValueVisitor<T> visitor);

    public static boolean isEncryptedValue(String value) {
        return value.startsWith(PREFIX);
    }

    public static EncryptedValue deserialize(String value) {
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Missing enc: prefix: " + value);
        }

        String suffix = value.substring(PREFIX.length());
        byte[] bytes = BaseEncoding.base64().decode(suffix);

        // this is a bit dubious but hopefully we can remove the legacy stuff soonish
        try {
            return MAPPER.readValue(bytes, EncryptedValue.class);
        } catch (IOException e) {
            return ImmutableLegacyEncryptedValue.of(bytes);
        }
    }

    @JsonIgnore
    public final String serialize() {
        byte[] bytes = accept(new EncryptedValueVisitor<byte[]>() {
            @Override
            public byte[] visit(LegacyEncryptedValue legacyEncryptedValue) {
                return legacyEncryptedValue.getCiphertext();
            }

            @Override
            public byte[] visit(AesEncryptedValue aesEncryptedValue) {
                try {
                    return MAPPER.writeValueAsBytes(aesEncryptedValue);
                } catch (JsonProcessingException e) {
                    throw Throwables.propagate(e);
                }
            }

            @Override
            public byte[] visit(RsaEncryptedValue rsaEncryptedValue) {
                try {
                    return MAPPER.writeValueAsBytes(rsaEncryptedValue);
                } catch (JsonProcessingException e) {
                    throw Throwables.propagate(e);
                }
            }
        });

        return PREFIX + BaseEncoding.base64().encode(bytes);
    }

    @JsonIgnore
    public final String getDecryptedValue(KeyWithAlgorithm kwa) {
        Algorithm algorithm = Algorithms.getInstance(kwa.getAlgorithm());
        return algorithm.getDecryptedString(this, kwa);
    }

    /**
     * Tries to decrypt using the key at the default path.
     * @see KeyPair#DEFAULT_PUBLIC_KEY_PATH
     * @return the decrypted value
     */
    @JsonIgnore
    public final String getDecryptedValue() {
        try {
            KeyPair keyPair = KeyPair.fromDefaultPath();
            // use private if we have it, else assume symmetric
            KeyWithAlgorithm kwa = keyPair.privateKey().or(keyPair.publicKey());
            return getDecryptedValue(kwa);
        } catch (IOException e) {
            throw new RuntimeException("Was unable to read key", e);
        }
    }
}
