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

package com.palantir.config.crypto.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.palantir.config.crypto.KeyFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;

public class EncryptedConfigMapperUtilsTest {

    private static final File CONFIG_FILE = new File("src/test/resources/testConfig.yml");
    private static final ObjectMapper MAPPER = new YAMLMapper();

    static {
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @Test
    public final void testCanDecryptValueInConfig() throws IOException {
        TestConfig config = EncryptedConfigMapperUtils.getConfig(CONFIG_FILE, TestConfig.class, MAPPER);

        assertThat(config.getUnencrypted()).isEqualTo("value");
        assertThat(config.getEncrypted()).isEqualTo("value");
        assertThat(config.getEncryptedWithSingleQuote()).isEqualTo("don't use quotes");
        assertThat(config.getEncryptedWithDoubleQuote()).isEqualTo("double quote is \"");
        assertThat(config.getEncryptedMalformedYaml()).isEqualTo("[oh dear");

        assertThat(config.getArrayWithSomeEncryptedValues())
                .containsExactly("value", "value", "other value", "[oh dear");
        assertThat(config.getPojoWithEncryptedValues()).satisfies(person -> {
            assertThat(person.getUsername()).isEqualTo("some-user");
            assertThat(person.getPassword()).isEqualTo("value");
        });
    }

    @Test
    public final void testCanDecryptValueInConfigFileContent() throws IOException {
        String configFileContent = Files.readString(CONFIG_FILE.toPath());
        TestConfig config = EncryptedConfigMapperUtils.getConfig(configFileContent, TestConfig.class, MAPPER);

        assertThat(config.getUnencrypted()).isEqualTo("value");
        assertThat(config.getEncrypted()).isEqualTo("value");
        assertThat(config.getEncryptedWithSingleQuote()).isEqualTo("don't use quotes");
        assertThat(config.getEncryptedWithDoubleQuote()).isEqualTo("double quote is \"");
        assertThat(config.getEncryptedMalformedYaml()).isEqualTo("[oh dear");

        assertThat(config.getArrayWithSomeEncryptedValues())
                .containsExactly("value", "value", "other value", "[oh dear");
        assertThat(config.getPojoWithEncryptedValues()).satisfies(person -> {
            assertThat(person.getUsername()).isEqualTo("some-user");
            assertThat(person.getPassword()).isEqualTo("value");
        });
    }

    @Value.Immutable
    @JsonSerialize(as = ImmutablePerson.class)
    @JsonDeserialize(as = ImmutablePerson.class)
    public interface Person {
        String getUsername();

        String getPassword();
    }

    public static final class TestConfig {
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
}
