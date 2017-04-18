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

package com.palantir.config.crypto.algorithm.rsa;

import com.palantir.config.crypto.ImmutableKeyWithType;
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyType;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public final class RsaKeyPair {

    private static final int KEY_SIZE_BITS = 2048;

    public static KeyPair newKeyPair() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(Algorithm.RSA.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(KEY_SIZE_BITS);

        java.security.KeyPair rsaKeyPair = keyPairGenerator.generateKeyPair();

        KeyWithType pub = ImmutableKeyWithType.builder()
                .type(KeyType.RSA_PUBLIC)
                .key(new RsaPublicKey(rsaKeyPair.getPublic()))
                .build();

        KeyWithType priv = ImmutableKeyWithType.builder()
                .type(KeyType.RSA_PRIVATE)
                .key(new RsaPrivateKey(rsaKeyPair.getPrivate()))
                .build();

        return KeyPair.of(pub, priv);
    }

    private RsaKeyPair() {
    }

}
