/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.config.crypto.algorithm.Algorithm;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class AesKeyTest {
    public static SecretKey newSecretKey() {
        javax.crypto.KeyGenerator keyGen;
        try {
            keyGen = javax.crypto.KeyGenerator.getInstance(Algorithm.AES.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGen.init(128);
        return keyGen.generateKey();
    }

    @Test
    public void testEqualityFromSameSecretKey() {
        SecretKey secretKey = newSecretKey();
        assertThat(new AesKey(secretKey)).isEqualTo(new AesKey(secretKey));
    }
}
