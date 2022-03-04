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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.palantir.config.crypto.jackson.JsonNodeStringReplacer;
import com.palantir.config.crypto.util.Person;
import com.palantir.config.crypto.util.TestConfig;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SubstitutingConfigurationFactoryTest {
    private static String previousProperty;
    private static SubstitutingConfigurationFactory<TestConfig> factory;

    @BeforeAll
    public static void beforeClass() throws IOException {
        previousProperty = System.getProperty(KeyFileUtils.KEY_PATH_PROPERTY);
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, "src/test/resources/test.key");

        factory = new SubstitutingConfigurationFactory<TestConfig>(
                TestConfig.class,
                Validators.newValidator(),
                Jackson.newObjectMapper(),
                "",
                new JsonNodeStringReplacer(new DecryptingVariableSubstitutor()));
    }

    @AfterAll
    public static void afterClass() {
        if (previousProperty != null) {
            System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, previousProperty);
        }
    }

    @Test
    public final void decryptionSucceeds() throws IOException, ConfigurationException {
        TestConfig config = factory.build(new File("src/test/resources/testConfig.yml"));

        assertThat(config.getEncrypted()).isEqualTo("value");
        assertThat(config.getUnencrypted()).isEqualTo("value");
        assertThat(config.getEncryptedWithSingleQuote()).isEqualTo("don't use quotes");
        assertThat(config.getEncryptedWithDoubleQuote()).isEqualTo("double quote is \"");
        assertThat(config.getEncryptedMalformedYaml()).isEqualTo("[oh dear");
        assertThat(config.getArrayWithSomeEncryptedValues())
                .containsExactly("value", "value", "other value", "[oh dear");
        assertThat(config.getPojoWithEncryptedValues()).isEqualTo(Person.of("some-user", "value"));
    }

    @Test
    public final void decryptionFailsWithNiceMessage() throws IOException, ConfigurationException {
        try {
            factory.build(new File("src/test/resources/testConfigWithError.yml"));
            failBecauseExceptionWasNotThrown(ConfigurationDecryptionException.class);
        } catch (ConfigurationDecryptionException e) {
            assertThat(e.getMessage()).contains("src/test/resources/testConfigWithError.yml has an error");
            assertThat(e.getMessage()).contains(
                    "The value 'enc:ERROR' for field 'arrayWithSomeEncryptedValues[3]' could not be replaced");
        }
    }
}
