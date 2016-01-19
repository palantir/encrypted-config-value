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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteStreams;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * A delegating {@link ConfigurationSourceProvider} which replaces variables in the underlying configuration
 * source according to the rules of a custom {@link org.apache.commons.lang3.text.StrSubstitutor}.
 */
public final class SubstitutingSourceProvider implements ConfigurationSourceProvider {
    private final ConfigurationSourceProvider delegate;
    private final StrSubstitutor substitutor;

    /**
     * Create a new instance.
     *
     * @param delegate    The underlying {@link io.dropwizard.configuration.ConfigurationSourceProvider}.
     * @param substitutor The custom {@link org.apache.commons.lang3.text.StrSubstitutor} implementation.
     */
    public SubstitutingSourceProvider(ConfigurationSourceProvider delegate, StrSubstitutor substitutor) {
        this.delegate = checkNotNull(delegate, "delegate cannot be null");
        this.substitutor = checkNotNull(substitutor, "substitutor cannot be null");
    }

    @Override
    public InputStream open(String path) throws IOException {
        try (final InputStream in = delegate.open(path)) {
            final String config = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
            final String substituted = substitutor.replace(config);

            return new ByteArrayInputStream(substituted.getBytes(StandardCharsets.UTF_8));
        }
    }
}
