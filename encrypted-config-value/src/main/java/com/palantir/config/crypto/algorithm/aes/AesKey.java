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

package com.palantir.config.crypto.algorithm.aes;

import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.ImmutableKeyWithType;
import com.palantir.config.crypto.Key;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyGenerator;
import com.palantir.config.crypto.algorithm.KeyType;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class AesKey implements Key {
    private final SecretKey secretKey;

    public AesKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Override
    public byte[] bytes() {
        return secretKey.getEncoded();
    }

    @Immutable
    public enum AesKeyGenerator implements KeyGenerator {
        INSTANCE;

        @Override
        public KeyWithType keyFromBytes(byte[] key) {
            SecretKeySpec localSecretKey = new SecretKeySpec(key, Algorithm.AES.toString());
            return ImmutableKeyWithType.builder()
                    .type(KeyType.AES)
                    .key(new AesKey(localSecretKey))
                    .build();
        }
    }
}
