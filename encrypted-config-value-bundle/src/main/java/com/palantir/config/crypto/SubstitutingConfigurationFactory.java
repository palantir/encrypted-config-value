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

/*
 * This file was derived from io.dropwizard.configuration.SubstitutingSourceProvider
 * https://github.com/dropwizard/dropwizard/blob/2183fd25d60f7f6da8ab3994ac27d8923098fd57/dropwizard-configuration/src/main/java/io/dropwizard/configuration/SubstitutingSourceProvider.java
 *
 * The original file is
 * Copyright 2010-2013 Coda Hale and Yammer, Inc., 2014-2015 Dropwizard Team
 * Licensed under the Apache License, Version 2.0
 *
 * The modifications to the original file are
 *  - make the class final (to pass checkstyle)
 *  - switch from Objects.requireNotNull to using Preconditions.checkNotNull
 */

package com.palantir.config.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.palantir.config.crypto.jackson.JsonNodeVisitor;
import com.palantir.config.crypto.jackson.JsonNodeVisitors;
import com.palantir.config.crypto.util.StringSubstitutionException;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import java.io.IOException;
import javax.validation.Validator;

/**
 * A {@link ConfigurationFactory} subclass which filters parsed JSON through a
 * {@link JsonNodeVisitor} before additional parsing.
 *
 * @param <T> the type of the configuration objects to produce
 */
public final class SubstitutingConfigurationFactory<T> extends ConfigurationFactory<T> {
    private final JsonNodeVisitor<JsonNode> substitutor;

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass the configuration class
     * @param validator the validator to use
     * @param objectMapper the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     * @param substitutor The custom {@link JsonNodeVisitor} implementation.
     */
    public SubstitutingConfigurationFactory(Class<T> klass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix,
            JsonNodeVisitor<JsonNode> substitutor) {
        super(klass, validator, objectMapper, propertyPrefix);
        this.substitutor = substitutor;
    }

    @Override
    protected T build(JsonNode node, String path) throws IOException, ConfigurationException {
        try {
            JsonNode substitutedNode = JsonNodeVisitors.dispatch(node, substitutor);
            return super.build(substitutedNode, path);
        } catch (StringSubstitutionException e) {
            String error = String.format(
                    "The value '%s' for field '%s' could not be replaced",
                    e.getValue(),
                    e.getField());
            throw new ConfigurationDecryptionException(path, ImmutableList.of(error), e);
        }
    }

    /**
     * A {@link ConfigurationFactoryFactory} which returns {@link SubstitutingConfigurationFactory}.
     *
     * @param <T> the type of the configuration objects to produce
     */
    public static final class Factory<T> implements ConfigurationFactoryFactory<T> {

        private final JsonNodeVisitor<JsonNode> substitutor;

        public Factory(JsonNodeVisitor<JsonNode> substitutor) {
            this.substitutor = substitutor;
        }

        @Override
        public ConfigurationFactory<T> create(Class<T> klass,
                Validator validator,
                ObjectMapper objectMapper,
                String propertyPrefix) {
            return new SubstitutingConfigurationFactory<>(klass, validator, objectMapper, propertyPrefix, substitutor);
        }
    }
}
