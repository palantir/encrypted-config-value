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
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import org.immutables.value.Value;

@Value.Immutable
public abstract class RsaPrivateKey implements Key {

    public abstract PrivateKey getPrivateKey();

    public static Key of(PrivateKey privateKey) {
        return ImmutableRsaPrivateKey.builder().privateKey(privateKey).build();
    }

    @Override
    public final byte[] bytes() {
        return getPrivateKey().getEncoded();
    }

    @Immutable
    public enum RsaPrivateKeyGenerator implements KeyGenerator {
        INSTANCE;

        @Override
        public KeyWithType keyFromBytes(byte[] key) {
            PrivateKey localPrivateKey;
            try {
                localPrivateKey =
                        KeyFactory.getInstance(Algorithm.RSA.toString()).generatePrivate(new PKCS8EncodedKeySpec(key));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return ImmutableKeyWithType.builder()
                    .type(KeyType.RSA_PRIVATE)
                    .key(RsaPrivateKey.of(localPrivateKey))
                    .build();
        }
    }
}
