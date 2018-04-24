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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import java.util.List;

public final class TestConfig extends Configuration {
    private final String unencrypted;
    private final String encrypted;
    private final String encryptedWithSingleQuote;
    private final String encryptedWithDoubleQuote;
    private final String encryptedMalformedYaml;
    private final List<String> arrayWithSomeEncryptedValues;
    private final Person pojoWithEncryptedValues;

    public TestConfig(
            @JsonProperty("unencrypted") String unencrypted,
            @JsonProperty("encrypted") String encrypted,
            @JsonProperty("encryptedWithSingleQuote") String encryptedWithSingleQuote,
            @JsonProperty("encryptedWithDoubleQuote") String encryptedWithDoubleQuote,
            @JsonProperty("encryptedMalformedYaml") String encryptedMalformedYaml,
            @JsonProperty("arrayWithSomeEncryptedValues") List<String> arrayWithSomeEncryptedValues,
            @JsonProperty("pojoWithEncryptedValues") Person pojoWithEncryptedValues) {
        this.unencrypted = unencrypted;
        this.encrypted = encrypted;
        this.encryptedWithSingleQuote = encryptedWithSingleQuote;
        this.encryptedWithDoubleQuote = encryptedWithDoubleQuote;
        this.encryptedMalformedYaml = encryptedMalformedYaml;
        this.arrayWithSomeEncryptedValues = arrayWithSomeEncryptedValues;
        this.pojoWithEncryptedValues = pojoWithEncryptedValues;
    }

    public String getUnencrypted() {
        return unencrypted;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public String getEncryptedWithSingleQuote() {
        return encryptedWithSingleQuote;
    }

    public String getEncryptedWithDoubleQuote() {
        return encryptedWithDoubleQuote;
    }

    public String getEncryptedMalformedYaml() {
        return encryptedMalformedYaml;
    }

    public List<String> getArrayWithSomeEncryptedValues() {
        return arrayWithSomeEncryptedValues;
    }

    public Person getPojoWithEncryptedValues() {
        return pojoWithEncryptedValues;
    }
}
