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

import com.palantir.config.crypto.util.TestConfig;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public final class VariableSubstitutionTest {

    static {
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    private static final DropwizardAppExtension<TestConfig> dropwizard =
            new DropwizardAppExtension<>(TestApplication.class, "src/test/resources/testConfig.yml");

    @Test
    public void testCanDecryptValueInConfig() {
        assertThat(dropwizard.getConfiguration().getUnencrypted()).isEqualTo("value");
        assertThat(dropwizard.getConfiguration().getEncrypted()).isEqualTo("value");
        assertThat(dropwizard.getConfiguration().getEncryptedWithSingleQuote()).isEqualTo("don't use quotes");
        assertThat(dropwizard.getConfiguration().getEncryptedWithDoubleQuote()).isEqualTo("double quote is \"");
        assertThat(dropwizard.getConfiguration().getEncryptedMalformedYaml()).isEqualTo("[oh dear");

        assertThat(dropwizard.getConfiguration().getArrayWithSomeEncryptedValues())
                .containsExactly("value", "value", "other value", "[oh dear");
        assertThat(dropwizard.getConfiguration().getPojoWithEncryptedValues()).satisfies(person -> {
            assertThat(person.getUsername()).isEqualTo("some-user");
            assertThat(person.getPassword()).isEqualTo("value");
        });
    }

    public static final class TestApplication extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new EncryptedConfigValueBundle());
        }

        @Override
        public void run(TestConfig _configuration, Environment _environment) {}
    }
}
