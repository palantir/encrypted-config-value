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

package com.palantir.config.crypto.algorithm;

import static com.google.common.base.Preconditions.checkArgument;

import com.palantir.config.crypto.Key;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.aes.AesKey;
import com.palantir.config.crypto.algorithm.rsa.RsaPrivateKey;
import com.palantir.config.crypto.algorithm.rsa.RsaPublicKey;

/**
 * KeyType defines the universe of available key types. Each key type has a unique name and supports creating a new
 * {@link KeyWithType} based on key bytes.
 */
public enum KeyType {
    AES("AES", AesKey.AesKeyGenerator.INSTANCE, Algorithm.AES),
    RSA_PUBLIC("RSA-PUB", RsaPublicKey.RsaPublicKeyGenerator.INSTANCE, Algorithm.RSA),
    RSA_PRIVATE("RSA-PRIV", RsaPrivateKey.RsaPrivateKeyGenerator.INSTANCE, Algorithm.RSA);

    public static KeyType from(String name) {
        for (KeyType alg : KeyType.values()) {
            if (alg.name.equals(name)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("unrecognized key algorithm: " + name);
    }

    private final String name;
    private final KeyGenerator generator;
    private final Algorithm algorithm;

    KeyType(String name, KeyGenerator generator, Algorithm algorithm) {
        this.name = name;
        this.generator = generator;
        this.algorithm = algorithm;
    }

    public String toString() {
        return name;
    }

    public KeyWithType keyFromBytes(byte[] keyBytes) {
        return this.generator.keyFromBytes(keyBytes);
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void checkKeyArgument(KeyWithType kwt, Class<? extends Key> keyClazz) {
        checkArgument(kwt.getType().equals(this),
                "key must be for %s algorithm but was %s", this, kwt.getType());
        checkArgument(keyClazz.isAssignableFrom(kwt.getKey().getClass()), "key must be of type %s but was %s", keyClazz,
                kwt.getKey().getClass());
    }

}
