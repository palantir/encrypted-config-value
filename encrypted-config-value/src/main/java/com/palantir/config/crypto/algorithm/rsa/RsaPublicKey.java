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

package com.palantir.config.crypto.algorithm.rsa;

import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.ImmutableKeyWithType;
import com.palantir.config.crypto.Key;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyGenerator;
import com.palantir.config.crypto.algorithm.KeyType;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public final class RsaPublicKey implements Key {
    private final PublicKey publicKey;

    public RsaPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public byte[] bytes() {
        return publicKey.getEncoded();
    }

    @Immutable
    public enum RsaPublicKeyGenerator implements KeyGenerator {
        INSTANCE;

        @Override
        public KeyWithType keyFromBytes(byte[] key) {
            PublicKey localPublicKey;
            try {
                localPublicKey =
                        KeyFactory.getInstance(Algorithm.RSA.toString()).generatePublic(new X509EncodedKeySpec(key));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return ImmutableKeyWithType.builder()
                    .type(KeyType.RSA_PUBLIC)
                    .key(new RsaPublicKey(localPublicKey))
                    .build();
        }
    }
}
