/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.config.crypto.algorithm.Algorithm;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import org.junit.Test;

public class RsaPrivateKeyTest {

    private static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(Algorithm.RSA.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

    @Test
    public void equalityFromSamePrivateKey() {
        PrivateKey privateKey = generateKeyPair().getPrivate();

        assertThat(RsaPrivateKey.of(privateKey)).isEqualTo(RsaPrivateKey.of(privateKey));
    }
}
