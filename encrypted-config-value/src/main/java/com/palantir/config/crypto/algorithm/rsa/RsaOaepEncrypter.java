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

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Encrypter;
import com.palantir.config.crypto.algorithm.KeyType;
import com.palantir.config.crypto.util.Suppliers;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Encrypts values using RSA-OAEP-MDF1. Uses SHA-256 as the hash function for both OAEP and MDF1.
 */
@Immutable
public enum RsaOaepEncrypter implements Encrypter {
    INSTANCE;

    private static final HashAlgorithm OAEP_HASH_ALG = HashAlgorithm.SHA256;
    private static final HashAlgorithm MDF1_HASH_ALG = HashAlgorithm.SHA256;

    public enum HashAlgorithm {
        SHA1("SHA-1"),
        SHA256("SHA-256");

        private final String name;

        HashAlgorithm(String name) {
            this.name = name;
        }

        @JsonValue
        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public final EncryptedValue encrypt(KeyWithType kwt, final String plaintext) {
        KeyType.RSA_PUBLIC.checkKeyArgument(kwt, RsaPublicKey.class);
        final PublicKey publicKey = ((RsaPublicKey) kwt.getKey()).getPublicKey();
        return Suppliers.silently(() -> {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(OAEP_HASH_ALG.toString(), "MGF1",
                    new MGF1ParameterSpec(
                            MDF1_HASH_ALG.toString()), PSource.PSpecified.DEFAULT);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey, oaepParams);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return ImmutableRsaEncryptedValue.builder()
                    .ciphertext(encrypted)
                    .oaepHashAlg(OAEP_HASH_ALG)
                    .mdf1HashAlg(MDF1_HASH_ALG)
                    .build();
        });
    }
}
