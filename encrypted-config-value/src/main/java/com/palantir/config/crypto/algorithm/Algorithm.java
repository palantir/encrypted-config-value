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

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Supplier;
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.algorithm.aes.AesGcmEncrypter;
import com.palantir.config.crypto.algorithm.aes.AesKeyPair;
import com.palantir.config.crypto.algorithm.rsa.RsaKeyPair;
import com.palantir.config.crypto.algorithm.rsa.RsaOaepEncrypter;

/**
 * Defines the known encryption algorithms. Algorithms can generate a new {@link KeyPair} that contains encryption and
 * decryption keys for the algorithm and can return an {@link Encrypter} that can be used to encrypt values using this
 * algorithm with a supported key.
 */
public enum Algorithm {
    AES("AES", () -> AesKeyPair.newKeyPair(), () -> AesGcmEncrypter.INSTANCE),
    RSA("RSA", () -> RsaKeyPair.newKeyPair(), () -> RsaOaepEncrypter.INSTANCE);

    private final String name;
    private final Supplier<KeyPair> keyPairGenerator;
    private final Supplier<Encrypter> encrypterGenerator;

    Algorithm(String name, Supplier<KeyPair> keyPairGenerator, Supplier<Encrypter> cipherGenerator) {
        this.name = name;
        this.keyPairGenerator = keyPairGenerator;
        this.encrypterGenerator = cipherGenerator;
    }

    public KeyPair newKeyPair() {
        return keyPairGenerator.get();
    }

    public Encrypter newEncrypter() {
        return encrypterGenerator.get();
    }

    @JsonValue
    public String toString() {
        return name;
    }
}
