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


import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.KeyWithType;

@Immutable
public interface Encrypter {
    /**
     * Creates an {@link EncryptedValue} that is the result of encrypting the provided plaintext using the provided key.
     * The returned {@link EncryptedValue} should contain information about the algorithm and paramters used to generate
     * the value. Throws an exception if the provided key cannot be used to generate an {@link EncryptedValue} for the
     * algorithm used by the {@link Encrypter}.
     */
    EncryptedValue encrypt(KeyWithType kwt, String plaintext);
}
