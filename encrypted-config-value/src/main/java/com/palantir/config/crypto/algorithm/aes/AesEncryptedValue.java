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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.EncryptedValueVisitor;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyType;
import com.palantir.config.crypto.util.Suppliers;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableAesEncryptedValue.class)
@JsonSerialize(as = AesEncryptedValue.class)
@Value.Style(additionalJsonAnnotations = JsonSerialize.class)
public abstract class AesEncryptedValue extends EncryptedValue {
    public enum Mode {
        GCM,
    }

    public final Algorithm getType() {
        return Algorithm.AES;
    }

    /**
     * Returns the encryption mode used by this encrypted value.
     */
    @Value.Default
    public Mode getMode() {
        return Mode.GCM;
    }

    public abstract byte[] getIv();

    public abstract byte[] getCiphertext();

    public abstract byte[] getTag();

    @Override
    public final String decrypt(KeyWithType kwt) {
        KeyType.AES.checkKeyArgument(kwt, AesKey.class);
        final SecretKey secretKeySpec = ((AesKey) kwt.getKey()).getSecretKey();
        return Suppliers.silently(() -> {
                    // Java expects the tag at the end of the encrypted bytes.
                    byte[] ct = Arrays.copyOf(getCiphertext(), getCiphertext().length + getTag().length);
                    System.arraycopy(getTag(), 0, ct, getCiphertext().length, getTag().length);

                    GCMParameterSpec gcmSpecWithIv = new GCMParameterSpec(getTag().length * Byte.SIZE, getIv());
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpecWithIv);

                    byte[] decrypted = cipher.doFinal(ct);
                    return new String(decrypted, StandardCharsets.UTF_8);
                }
        );
    }

    @Override
    public final <T> T accept(EncryptedValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
