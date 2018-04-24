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

package com.palantir.config.crypto.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSubstitutionExceptionTest {
    private static final String VALUE = "abc";

    @Mock
    private Throwable cause;

    @Test
    public final void testConstructions() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception, "");
    }

    @Test
    public final void testSingleExtendField() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend("field1"), "field1");
    }

    @Test
    public final void testDoubleExtendField() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend("field1").extend("field2"), "field2.field1");
    }

    @Test
    public final void testSingleExtendArray() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend(1), "[1]");
    }

    @Test
    public final void testDoubleExtendArray() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend(1).extend(2), "[2][1]");
    }

    @Test
    public final void testSingleExtendArrayAndField() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend(1).extend("field"), "field[1]");
    }

    @Test
    public final void testSingleExtendArrayAndDoubleField() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend(1).extend("field1").extend("field2"), "field2.field1[1]");
    }

    @Test
    public final void testSingleExtendArrayBetweenDoubleField() {
        StringSubstitutionException exception = new StringSubstitutionException(cause, VALUE);
        assertException(exception.extend("field1").extend(1).extend("field2"), "field2[1].field1");
    }

    private void assertException(StringSubstitutionException exception, String field) {
        assertThat(exception.getValue(), is(VALUE));
        assertThat(exception.getField(), is(field));
        assertThat(exception.getCause(), is(cause));
    }
}
