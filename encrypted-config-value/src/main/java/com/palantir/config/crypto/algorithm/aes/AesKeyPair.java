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

package com.palantir.config.crypto.algorithm.aes;

import com.palantir.config.crypto.ImmutableKeyWithType;
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyType;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;

public final class AesKeyPair {

    private static final int KEY_SIZE_BITS = 256;

    public static KeyPair newKeyPair() {
        javax.crypto.KeyGenerator keyGen;
        try {
            keyGen = javax.crypto.KeyGenerator.getInstance(Algorithm.AES.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.init(KEY_SIZE_BITS);
        SecretKey secretKey = keyGen.generateKey();

        KeyWithType kwa = ImmutableKeyWithType.builder()
                .type(KeyType.AES)
                .key(new AesKey(secretKey))
                .build();
        return KeyPair.symmetric(kwa);
    }

    private AesKeyPair() {
    }
}
