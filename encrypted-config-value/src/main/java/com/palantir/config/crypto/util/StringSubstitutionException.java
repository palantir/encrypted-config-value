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

package com.palantir.config.crypto.util;

public class StringSubstitutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String value;
    private final StringBuilder fieldBuilder;
    private final boolean lastExtensionWasArrayIndex;

    private StringSubstitutionException(
            Throwable cause, String value, StringBuilder fieldBuilder, boolean lastExtensionWasArrayIndex) {
        super(cause);
        this.value = value;
        this.fieldBuilder = fieldBuilder;
        this.lastExtensionWasArrayIndex = lastExtensionWasArrayIndex;
    }

    public StringSubstitutionException(Throwable cause, String value) {
        this(cause, value, new StringBuilder(), false);
    }

    public final StringSubstitutionException extend(String field) {
        return extend(field, false);
    }

    public final StringSubstitutionException extend(int arrayIndex) {
        return extend("[" + arrayIndex + "]", true);
    }

    private StringSubstitutionException extend(String prefix, boolean prefixIsArrayIndex) {
        StringBuilder extendedFieldBuilder = new StringBuilder(prefix);
        if (fieldBuilder.length() > 0 && !lastExtensionWasArrayIndex) {
            extendedFieldBuilder.append(".");
        }
        extendedFieldBuilder.append(fieldBuilder);
        return new StringSubstitutionException(getCause(), value, extendedFieldBuilder, prefixIsArrayIndex);
    }

    public final String getField() {
        return fieldBuilder.toString();
    }

    public final String getValue() {
        return value;
    }
}
