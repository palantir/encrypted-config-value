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

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.palantir.config.crypto.util.TestConfig;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;

public final class VariableSubstitutionTest {

    static {
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE =
            new DropwizardAppRule(TestApplication.class, "src/test/resources/testConfig.yml");

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

    public static final class TestApplication extends Application<TestConfig> {
        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new EncryptedConfigValueBundle());
        }

        @Override
        public void run(TestConfig _configuration, Environment _environment) throws Exception {}
    }

}
