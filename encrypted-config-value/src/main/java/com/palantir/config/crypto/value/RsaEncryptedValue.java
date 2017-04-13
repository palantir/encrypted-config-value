/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.config.crypto.value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRsaEncryptedValue.class)
@JsonDeserialize(as = ImmutableRsaEncryptedValue.class)
@JsonTypeName("RSA")
public abstract class RsaEncryptedValue extends EncryptedValue {
    public static RsaEncryptedValue fromLegacy(LegacyEncryptedValue value) {
        return ImmutableRsaEncryptedValue.of(value.getCiphertext());
    }

    @Value.Parameter
    public abstract byte[] getCiphertext();

    @Override
    public final <T> T accept(EncryptedValueVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
