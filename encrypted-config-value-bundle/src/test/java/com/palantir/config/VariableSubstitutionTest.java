/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

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
        System.setProperty(KeyWithAlgorithm.KEY_PATH_PROPERTY, "src/test/resources/test.key");
    }

    @ClassRule
    public static final DropwizardAppRule<TestConfig> RULE =
            new DropwizardAppRule<TestConfig>(TestApplication.class, "src/test/resources/testConfig.yml");

    @Test
    public void testCanDecryptValueInConfig() throws IOException {
        assertEquals("value", RULE.getConfiguration().getUnencrypted());
        assertEquals("value", RULE.getConfiguration().getEncrypted());
        assertEquals("value", RULE.getConfiguration().getValueObject().getDecryptedValue());
    }

    public static final class TestConfig extends Configuration {
        private final String unencrypted;
        private final String encrypted;
        private final EncryptedConfigValue valueObject;

        public TestConfig(
                @JsonProperty("unencrypted") String unencrypted,
                @JsonProperty("encrypted") String encrypted,
                @JsonProperty("value-object") EncryptedConfigValue valueObject) {
            this.unencrypted = unencrypted;
            this.encrypted = encrypted;
            this.valueObject = valueObject;
        }

        public String getUnencrypted() {
            return unencrypted;
        }

        public String getEncrypted() {
            return encrypted;
        }

        public EncryptedConfigValue getValueObject() {
            return valueObject;
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
