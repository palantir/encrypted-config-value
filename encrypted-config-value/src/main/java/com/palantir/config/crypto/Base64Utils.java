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

package com.palantir.config.crypto;

import com.google.common.io.BaseEncoding;

public final class Base64Utils {

    private Base64Utils() {
        /* do not instantiate */
    }

    /**
     * Checks whether the given value is valid base-64 and otherwise throws IllegalArgumentException.
     * @param value  the value to check
     */
    @SuppressWarnings("CheckReturnValue")
    public static void checkIsBase64(String value) {
        // there are various edge cases
        // the regexes people claim to work are actually incorrect
        // implementations also differ in quite how they handle left-over bits/padding
        // since we use the Guava base64 decoder, we'll claim it's valid if we can decode it
        try {
            BaseEncoding.base64().decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("value " + value + " is not valid base64", e);
        }
    }
}
