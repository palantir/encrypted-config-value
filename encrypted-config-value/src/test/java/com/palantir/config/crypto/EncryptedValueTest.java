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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.palantir.config.crypto.algorithm.AesAlgorithm;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public final class EncryptedValueTest {
    private static final String plaintext = "my secret. I don't want anyone to know this";
    private static final KeyWithAlgorithm aesKey = KeyWithAlgorithm.fromString("AES:rqrvWpLld+wKLOyxJYxQVg==");

    @Test
    public void weCanConstructFromAValidString() {
        System.out.println(aesKey);
        String valid = "enc:QjR4AHIYoIzvjEHf53XETM3QYnCl1mgFYC51Q7x4ebwM+h3PHVqSt/"
                + "1un/+KvpJ2mZfMH0tifu+htRVxEPyXmt88lyKB83NpesNJEoLFLL+wBWCkppaLRuc/1w==";

        EncryptedValue encryptedValue = EncryptedValue.of(valid);
        assertThat(encryptedValue.getDecryptedValue(aesKey), is(plaintext));
    }

    @Test(expected = IllegalArgumentException.class)
    public void weFailToConstructWithInvalidPrefix() {
        String invalid = "anc:TCkE/OT7xsKWqP4SRNBEj54Pk7wDMQzMGJtX90toFuGeejM/LQBDTZ8hEaKQt/3i";
        EncryptedValue.of(invalid); // throws
    }

    @Test(expected = IllegalArgumentException.class)
    public void weFailToConstructWithAnInvalidEncryptedValue() {
        String invalid = "enc:verysecret";
        EncryptedValue.fromEncryptedString(invalid);
    }

    @Test(expected = RuntimeException.class)
    public void weCannotDecryptWithTheWrongKey() throws NoSuchAlgorithmException {
        KeyWithAlgorithm key = KeyWithAlgorithm.randomKey("AES");
        KeyWithAlgorithm otherKey = KeyWithAlgorithm.randomKey("AES");

        EncryptedValue encryptedValue = new AesAlgorithm().getEncryptedValue(plaintext, key);

        encryptedValue.getDecryptedValue(otherKey); //throws
    }
}
