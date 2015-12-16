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

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
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
    }

    public static final class TestConfig extends Configuration {
        private final String unencrypted;
        private final String encrypted;

        public TestConfig(
                @JsonProperty("unencrypted") String unencrypted,
                @JsonProperty("encrypted") String encrypted) {
            this.unencrypted = unencrypted;
            this.encrypted = encrypted;
        }

        public String getUnencrypted() {
            return unencrypted;
        }

        public String getEncrypted() {
            return encrypted;
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
