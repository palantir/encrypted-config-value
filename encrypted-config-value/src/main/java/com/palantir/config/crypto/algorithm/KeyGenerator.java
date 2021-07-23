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

package com.palantir.config.crypto.algorithm;

import com.google.errorprone.annotations.Immutable;
import com.palantir.config.crypto.Key;
import com.palantir.config.crypto.KeyWithType;

@Immutable
public interface KeyGenerator {
    /**
     * Creates a {@link KeyWithType} based on the provided key content bytes. The provided bytes should correspond to
     * the bytes returned by {@link Key#bytes()}. The type of the key is supplied by the implementor of the interface.
     */
    KeyWithType keyFromBytes(byte[] key);
}
