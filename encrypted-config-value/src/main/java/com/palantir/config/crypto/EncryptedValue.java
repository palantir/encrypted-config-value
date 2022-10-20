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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.palantir.config.crypto.algorithm.aes.AesEncryptedValue;
import com.palantir.config.crypto.algorithm.rsa.RsaEncryptedValue;
import com.palantir.logsafe.exceptions.SafeRuntimeException;
import java.io.IOException;

/**
 * A value that has been encrypted using an algorithm with specific parameters. The value can be decrypted when provided
 * with a key that has a type that is capable of performing decryption for the algorithm used to encrypt this value.
 * The serializable String form is "enc:base64-encoded-value".
 *
 * An {@link EncryptedValue} has a legacy format and a current format.
 *
 * In the legacy format, the base64-encoded-value is the base64-encoded ciphertext bytes. The value does not contain any
 * information about the algorithm or parameters used to encrypt it and blindly uses any key provided to attempt to
 * decrypt the ciphertext.
 *
 * In the current format, the base64-encoded-value is the base64-encoded JSON representation of the concrete
 * {@link EncryptedValue} subclass of the value. The subclass contains information about the algorithm used to encrypt
 * the value, along with any relevant parameters for the algorithm.
 */
@JsonSubTypes({
    @JsonSubTypes.Type(value = AesEncryptedValue.class, name = "AES"),
    @JsonSubTypes.Type(value = RsaEncryptedValue.class, name = "RSA")
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
public abstract class EncryptedValue {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PREFIX = "enc:";

    @JsonIgnore
    public abstract <T> T accept(EncryptedValueVisitor<T> visitor);

    @JsonIgnore
    public abstract String decrypt(KeyWithType kwa);

    public static boolean isEncryptedValue(String value) {
        return value.startsWith(PREFIX);
    }

    public static EncryptedValue fromString(String value) {
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Missing \"enc:\" prefix: " + value);
        }

        String suffix = value.substring(PREFIX.length());

        byte[] bytes = BaseEncoding.base64().decode(suffix);

        // this is a bit dubious, but hopefully we can remove the legacy stuff soon-ish
        try {
            return MAPPER.readValue(bytes, EncryptedValue.class);
        } catch (IOException e) {
            // TODO(nmiyake): add logging in this case?
            // if reading as JSON fails, assume that it is a legacy value
            return ImmutableLegacyEncryptedValue.of(bytes);
        }
    }

    @Override
    public final String toString() {
        byte[] bytes = accept(new EncryptedValueVisitor<byte[]>() {
            @Override
            public byte[] visit(LegacyEncryptedValue legacyEncryptedValue) {
                return legacyEncryptedValue.getCiphertext();
            }

            @Override
            public byte[] visit(AesEncryptedValue aesEncryptedValue) {
                return getJsonBytes(aesEncryptedValue);
            }

            @Override
            public byte[] visit(RsaEncryptedValue rsaEncryptedValue) {
                return getJsonBytes(rsaEncryptedValue);
            }
        });
        return PREFIX + BaseEncoding.base64().encode(bytes);
    }

    private static byte[] getJsonBytes(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SafeRuntimeException(e);
        }
    }
}
