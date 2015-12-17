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

public final class Algorithms {

    private Algorithms() {
        /* do not instantiate */
    }

    public static Algorithm getInstance(String algorithmType) {
        switch (algorithmType) {
            case AesAlgorithm.ALGORITHM_TYPE:
                return new AesAlgorithm();
            case RsaAlgorithm.ALGORITHM_TYPE:
                return new RsaAlgorithm();
            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + algorithmType);
        }
    }
}
