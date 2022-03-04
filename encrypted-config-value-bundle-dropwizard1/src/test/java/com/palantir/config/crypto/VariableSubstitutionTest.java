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
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import com.palantir.config.crypto.util.TestConfig;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
import org.assertj.core.api.HamcrestCondition;
import org.junit.ClassRule;
import org.junit.Test;

public final class VariableSubstitutionTest {

    static {
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE =
            new DropwizardAppRule(TestApplication.class, "src/test/resources/testConfig.yml");

    @Test
    public void testCanDecryptValueInConfig() throws IOException {
        assertThat(RULE.getConfiguration().getUnencrypted()).isEqualTo("value");
        assertThat(RULE.getConfiguration().getEncrypted()).isEqualTo("value");
        assertThat(RULE.getConfiguration().getEncryptedWithSingleQuote()).isEqualTo("don't use quotes");
        assertThat(RULE.getConfiguration().getEncryptedWithDoubleQuote()).isEqualTo("double quote is \"");
        assertThat(RULE.getConfiguration().getEncryptedMalformedYaml()).isEqualTo("[oh dear");

        assertThat(RULE.getConfiguration().getArrayWithSomeEncryptedValues())
                .containsExactly("value", "value", "other value", "[oh dear");
        assertThat(RULE.getConfiguration().getPojoWithEncryptedValues())
                .is(new HamcrestCondition<>(both(hasProperty("username", equalTo("some-user")))
                        .and(hasProperty("password", equalTo("value")))));
    }

    public static final class TestApplication extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new EncryptedConfigValueBundle());
        }

        @Override
        public void run(TestConfig _configuration, Environment _environment) throws Exception {}
    }
}
