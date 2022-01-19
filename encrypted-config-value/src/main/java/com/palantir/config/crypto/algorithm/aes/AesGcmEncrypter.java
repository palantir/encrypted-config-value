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

import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Encrypter;
import com.palantir.config.crypto.algorithm.KeyType;
import com.palantir.config.crypto.util.Suppliers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@Immutable
public enum AesGcmEncrypter implements Encrypter {
    INSTANCE;

    private static final int IV_SIZE_BITS = 96;
    private static final int TAG_SIZE_BITS = 128;

    @Override
    public final EncryptedValue encrypt(KeyWithType kwt, final String plaintext) {
        KeyType.AES.checkKeyArgument(kwt, AesKey.class);
        final SecretKey secretKeySpec = ((AesKey) kwt.getKey()).getSecretKey();

        return Suppliers.silently(() -> {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");

            byte[] ivBytes = new byte[IV_SIZE_BITS / Byte.SIZE];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivBytes);

            GCMParameterSpec gcmSpecWithIv = new GCMParameterSpec(TAG_SIZE_BITS, ivBytes);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpecWithIv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Java always appends tag to ciphertext, so split apart manually
            byte[] ciphertext = Arrays.copyOfRange(encrypted, 0, encrypted.length - (TAG_SIZE_BITS / Byte.SIZE));
            byte[] tag =
                    Arrays.copyOfRange(encrypted, encrypted.length - (TAG_SIZE_BITS / Byte.SIZE), encrypted.length);

            return ImmutableAesEncryptedValue.builder()
                    .iv(ivBytes)
                    .ciphertext(ciphertext)
                    .tag(tag)
                    .build();
        });
    }
}
