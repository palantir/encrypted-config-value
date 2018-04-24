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

package com.palantir.config.crypto;


import com.palantir.config.crypto.algorithm.aes.AesEncryptedValue;
import com.palantir.config.crypto.algorithm.aes.AesKey;
import com.palantir.config.crypto.algorithm.aes.ImmutableAesEncryptedValue;
import com.palantir.config.crypto.algorithm.rsa.ImmutableRsaEncryptedValue;
import com.palantir.config.crypto.algorithm.rsa.RsaEncryptedValue;
import com.palantir.config.crypto.algorithm.rsa.RsaOaepEncrypter;
import com.palantir.config.crypto.algorithm.rsa.RsaPrivateKey;
import java.util.Arrays;
import org.immutables.value.Value;

@Value.Immutable
public abstract class LegacyEncryptedValue extends EncryptedValue {

    @Value.Parameter
    public abstract byte[] getCiphertext();

    @Override
    public final String decrypt(KeyWithType kwa) {
        EncryptedValue translatedValue;
        if (kwa.getKey() instanceof AesKey) {
            // if AES key is provided, interpret value as legacy AES value
            translatedValue = aesValueFromLegacy(this);
        } else if (kwa.getKey() instanceof RsaPrivateKey) {
            // if RSA key is provided, interpret value as legacy RSA value
            translatedValue = rsaValueFromLegacy(this);
        } else {
            throw new IllegalArgumentException(
                    "decrypting legacy values not supported for key type " + kwa.getKey().getClass());
        }
        return translatedValue.decrypt(kwa);
    }

    private static final int LEGACY_IV_SIZE = 256 / Byte.SIZE;
    private static final int LEGACY_TAG_SIZE = 128 / Byte.SIZE;

    private static AesEncryptedValue aesValueFromLegacy(LegacyEncryptedValue value) {
        byte[] buf = value.getCiphertext();
        byte[] iv = Arrays.copyOfRange(buf, 0, LEGACY_IV_SIZE);
        byte[] ct = Arrays.copyOfRange(buf, LEGACY_IV_SIZE, buf.length - LEGACY_TAG_SIZE);
        byte[] tag = Arrays.copyOfRange(buf, buf.length - LEGACY_TAG_SIZE, buf.length);

        return ImmutableAesEncryptedValue.builder()
                .iv(iv)
                .ciphertext(ct)
                .tag(tag)
                .build();
    }

    private static RsaEncryptedValue rsaValueFromLegacy(LegacyEncryptedValue value) {
        return ImmutableRsaEncryptedValue.builder()
                .ciphertext(value.getCiphertext())
                .oaepHashAlg(RsaOaepEncrypter.HashAlgorithm.SHA256)
                .mdf1HashAlg(RsaOaepEncrypter.HashAlgorithm.SHA1)
                .build();
    }

    @Override
    public final <T> T accept(EncryptedValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
