/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
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

import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.KeyWithAlgorithm;

/**
 * Explicitly support a defined set of algorithms, so we can choose sane defaults etc.
 */
public interface Algorithm {

    KeyPair generateKey();

    EncryptedValue getEncryptedValue(String plaintext, KeyWithAlgorithm kwa);

    String getDecryptedString(EncryptedValue encryptedValue, KeyWithAlgorithm kwa);

    String getName();
}
