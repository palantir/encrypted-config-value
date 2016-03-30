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

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
import java.util.List;
import org.immutables.value.Value;
import org.junit.ClassRule;
import org.junit.Test;

public final class VariableSubstitutionTest {

    static {
        System.setProperty(KeyPair.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE =
            new DropwizardAppRule<TestConfig>(TestApplication.class, "src/test/resources/testConfig.yml");

    @Test
    public void testCanDecryptValueInConfig() throws IOException {
        assertEquals("value", RULE.getConfiguration().getUnencrypted());
        assertEquals("value", RULE.getConfiguration().getEncrypted());
        assertEquals("don't use quotes", RULE.getConfiguration().getEncryptedWithSingleQuote());
        assertEquals("double quote is \"", RULE.getConfiguration().getEncryptedWithDoubleQuote());
        assertEquals("[oh dear", RULE.getConfiguration().getEncryptedMalformedYaml());

        assertThat(RULE.getConfiguration().getArrayWithSomeEncryptedValues(),
                contains("value", "value", "other value", "[oh dear"));
        assertThat(RULE.getConfiguration().getPojoWithEncryptedValues(),
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

    public static final class TestConfig extends Configuration {
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

    public static final class TestApplication extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new EncryptedConfigValueBundle());
        }

        @Override
        public void run(TestConfig configuration, Environment environment) throws Exception {}
    }

}
