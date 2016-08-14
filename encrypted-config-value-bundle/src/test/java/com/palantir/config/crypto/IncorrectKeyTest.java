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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.config.crypto.SubstitutingConfigurationFactory.Factory;
import com.palantir.config.crypto.VariableSubstitutionTest.Person;
import com.palantir.config.crypto.jackson.JsonNodeStringReplacer;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.File;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.immutables.value.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class IncorrectKeyTest {

    static {
        System.setProperty(KeyPair.KEY_PATH_PROPERTY, "src/test/resources/bad.key");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testErrorMessageIsNiceForOneLevelConfigEntry() throws IOException, ConfigurationException {
        JsonNodeStringReplacer replacer = new JsonNodeStringReplacer(new DecryptingVariableSubstitutor());
        Factory<TestConfig1> honk = new SubstitutingConfigurationFactory.Factory<TestConfig1>(replacer);
        ConfigurationFactory<TestConfig1> factory = honk.create(
                TestConfig1.class, Validators.newValidator(), Jackson.newObjectMapper(), "");

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage(Matchers.containsString(
                "src/test/resources/testConfig.yml has an error"));
        expectedException.expectMessage(Matchers.containsString(
                "The encrypted value for key 'encrypted' could not be decrypted"));

        factory.build(new File("src/test/resources/testConfig.yml"));
    }

    @Test
    public void testErrorMessageIsNiceForMultiLevelConfigEntry() throws IOException, ConfigurationException {
        JsonNodeStringReplacer replacer = new JsonNodeStringReplacer(new DecryptingVariableSubstitutor());
        Factory<TestConfig2> honk = new SubstitutingConfigurationFactory.Factory<TestConfig2>(replacer);
        ConfigurationFactory<TestConfig2> factory = honk.create(
                TestConfig2.class, Validators.newValidator(), Jackson.newObjectMapper(), "");

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage(Matchers.containsString(
                "src/test/resources/pojoOnlyConfig.yml has an error"));
        expectedException.expectMessage(Matchers.containsString(
                "The encrypted value for key 'pojoWithEncryptedValues.password' could not be decrypted"));

        factory.build(new File("src/test/resources/pojoOnlyConfig.yml"));
    }


    @Value.Immutable
    @JsonSerialize(as = UserAndPass.class)
    @JsonDeserialize(as = UserAndPass.class)
    public interface UserAndPass {
        String getUsername();

        String getPassword();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class TestConfig1 extends Configuration {
        private final String encrypted;

        public TestConfig1(
                @JsonProperty("encrypted") String encrypted) {
            this.encrypted = encrypted;
        }

        public String getEncrypted() {
            return encrypted;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class TestConfig2 extends Configuration {
        private final Person pojoWithEncryptedValues;

        public TestConfig2(
                @JsonProperty("pojoWithEncryptedValues") Person pojoWithEncryptedValues) {
            this.pojoWithEncryptedValues = pojoWithEncryptedValues;
        }

        public Person getPojoWithEncryptedValues() {
            return pojoWithEncryptedValues;
        }
    }
}
