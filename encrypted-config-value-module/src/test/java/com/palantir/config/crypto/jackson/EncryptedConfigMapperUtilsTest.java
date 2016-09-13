/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config.crypto.jackson;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.palantir.config.crypto.KeyPair;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.immutables.value.Value;
import org.junit.Test;

public class EncryptedConfigMapperUtilsTest {

    private static final File CONFIG_FILE = new File("src/test/resources/testConfig.yml");
    private static final ObjectMapper MAPPER = new YAMLMapper();

    static {
        System.setProperty(KeyPair.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @Test
    public final void testCanDecryptValueInConfig() throws IOException {

        TestConfig config = EncryptedConfigMapperUtils.getConfig(CONFIG_FILE, TestConfig.class, MAPPER);

        assertEquals("value", config.getUnencrypted());
        assertEquals("value", config.getEncrypted());
        assertEquals("don't use quotes", config.getEncryptedWithSingleQuote());
        assertEquals("double quote is \"", config.getEncryptedWithDoubleQuote());
        assertEquals("[oh dear", config.getEncryptedMalformedYaml());

        assertThat(config.getArrayWithSomeEncryptedValues(),
                contains("value", "value", "other value", "[oh dear"));
        assertThat(config.getPojoWithEncryptedValues(),
                both(hasProperty("username", equalTo("some-user")))
                .and(hasProperty("password", equalTo("value"))));
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
