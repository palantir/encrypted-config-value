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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.EncryptedValueVisitor;
import com.palantir.config.crypto.KeyWithType;
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.KeyType;
import com.palantir.config.crypto.util.Suppliers;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableRsaEncryptedValue.class)
@JsonSerialize(as = RsaEncryptedValue.class)
public abstract class RsaEncryptedValue extends EncryptedValue {

    public enum Mode {
        OAEP,
    }

    public final Algorithm getType() {
        return Algorithm.RSA;
    }

    /**
     * Returns the encryption mode used by this encrypted value.
     */
    @Value.Default
    public Mode getMode() {
        return Mode.OAEP;
    }

    public abstract byte[] getCiphertext();

    @JsonProperty("oaep-alg")
    public abstract RsaOaepEncrypter.HashAlgorithm getOaepHashAlg();

    @JsonProperty("mdf1-alg")
    public abstract RsaOaepEncrypter.HashAlgorithm getMdf1HashAlg();

    @Override
    public final String decrypt(KeyWithType kwt) {
        KeyType.RSA_PRIVATE.checkKeyArgument(kwt, RsaPrivateKey.class);
        final PrivateKey privateKey = ((RsaPrivateKey) kwt.getKey()).getPrivateKey();
        return Suppliers.silently(() -> {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    getOaepHashAlg().toString(),
                    "MGF1",
                    new MGF1ParameterSpec(getMdf1HashAlg().toString()),
                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

            byte[] decrypted = cipher.doFinal(getCiphertext());
            return new String(decrypted, StandardCharsets.UTF_8);
        });
    }

    @Override
    public final <T> T accept(EncryptedValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
