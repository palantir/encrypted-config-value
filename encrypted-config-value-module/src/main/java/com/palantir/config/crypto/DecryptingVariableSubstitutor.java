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

import com.palantir.config.crypto.jackson.Substitutor;
import com.palantir.config.crypto.util.StringSubstitutionException;

public final class DecryptingVariableSubstitutor implements Substitutor {
    private static final String PREFIX = "${" + EncryptedValue.PREFIX;
    private static final String SUFFIX = "}";

    public DecryptingVariableSubstitutor() {}

    @Override
    public String replace(String source) {
        if (source != null) {
            int index = source.indexOf(PREFIX);
            if (index >= 0) {
                StringBuilder result = new StringBuilder(source);
                do {
                    int end = result.indexOf(SUFFIX, index);
                    if (end < 0) {
                        break;
                    }

                    String value = result.substring(index + PREFIX.length() - EncryptedValue.PREFIX.length(), end);
                    if (EncryptedValue.isEncryptedValue(value)) {
                        String decrypted = decrypt(value);
                        result.replace(index, end + SUFFIX.length(), decrypted);
                        index = result.indexOf(PREFIX, index + decrypted.length());
                    } else {
                        index = result.indexOf(PREFIX, end);
                    }
                } while (index >= 0);
                return result.toString();
            }
        }
        return source;
    }

    private static String decrypt(String encryptedValue) {
        try {
            return KeyFileUtils.decryptUsingDefaultKeys(EncryptedValue.fromString(encryptedValue));
        } catch (RuntimeException e) {
            throw new StringSubstitutionException(e, encryptedValue);
        }
    }
}
