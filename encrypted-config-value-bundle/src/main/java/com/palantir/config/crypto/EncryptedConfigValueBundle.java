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

import com.fasterxml.jackson.databind.JsonNode;
import com.palantir.config.crypto.jackson.JsonNodeStringReplacer;
import com.palantir.config.crypto.jackson.JsonNodeVisitor;
import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public final class EncryptedConfigValueBundle implements Bundle {

    // Generically capture configuration type T from Bootstrap<T>, though we don't actually care about it
    private static <T extends Configuration> void setConfigurationFactoryFactory(
            Bootstrap<T> bootstrap,
            final JsonNodeVisitor<JsonNode> replacer) {
        bootstrap.setConfigurationFactoryFactory(new SubstitutingConfigurationFactory.Factory<T>(replacer));
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addCommand(new GenerateKeyCommand());
        bootstrap.addCommand(new EncryptConfigValueCommand());
        setConfigurationFactoryFactory(bootstrap, new JsonNodeStringReplacer(new DecryptingVariableSubstitutor()));
    }

    @Override
    public void run(Environment environment) {}

}
