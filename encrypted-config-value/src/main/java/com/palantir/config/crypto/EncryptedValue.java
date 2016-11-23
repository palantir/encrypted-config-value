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

import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.Algorithms;
import java.io.IOException;
import org.immutables.value.Value;

@Value.Immutable
public abstract class EncryptedValue {

    private static final String PREFIX = "enc:";

    public abstract String encryptedValue();

    @Value.Check
    protected final void checkEncryptedValueIsValid() {
        Base64Utils.checkIsBase64(encryptedValue());
    }

    public final String getDecryptedValue(KeyWithAlgorithm kwa) {
        Algorithm algorithm = Algorithms.getInstance(kwa.getAlgorithm());
        return algorithm.getDecryptedString(this, kwa);
    }

    /**
     * Tries to decrypt using the key at the default path.
     * @see KeyPair#DEFAULT_PUBLIC_KEY_PATH
     * @return the decrypted value
     */
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

    @Override
    public final String toString() {
        return PREFIX + encryptedValue();
    }

    public static boolean isEncryptedValue(String encryptedValue) {
        return encryptedValue.startsWith(PREFIX);
    }

    /**
     * @param encryptedValue - a string of the form "prefix:encrypted-string-in-base-64".
     * @return an EncryptedConfigValue for this encrypted string
     */
    public static EncryptedValue of(String encryptedValue) {
        checkArgument(isEncryptedValue(encryptedValue),
                "encrypted value must begin with %s but is %s", PREFIX, encryptedValue);
        return fromEncryptedString(encryptedValue.substring(PREFIX.length()));
    }

    /**
     * @param encryptedString - an encrypted string to wrap.
     * @return an EncryptedConfigValue for this encrypted string
     */
    public static EncryptedValue fromEncryptedString(String encryptedString) {
        return ImmutableEncryptedValue.builder()
                .encryptedValue(encryptedString)
                .build();
    }
}
