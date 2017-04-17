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
import com.palantir.config.crypto.algorithm.Algorithm;
import com.palantir.config.crypto.algorithm.RsaAlgorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public final class EncryptedValueTest {
    private static final String plaintext = "my secret. I don't want anyone to know this";
    private static final KeyWithAlgorithm aesKey = KeyWithAlgorithm.fromString("AES:rqrvWpLld+wKLOyxJYxQVg==");
    private static final KeyWithAlgorithm rsaPubKey =
            KeyWithAlgorithm.fromString("RSA:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzfqeBMGXjHTqrOc+Ew1"
                    + "nEWjLdNxBBKtHXh5WmFDth6ZrUeQ832a07k2zDC7nhoFfIcI+XJ14Wfp93jBvOo2dixrWFpm8qODcEz407Y89"
                    + "u7/L5C87sGB7Fsauo2wqeupXjoY2BWHn6Z9JgNtuEuFaQR0jTkxJd8+loORQGiBfU+1UZ0/8WWLdihTVnpYGQ"
                    + "Z4M1khOgdYPZ1iKK8Vh6wsoAqSjkxoHyqFWKkIU/qSrpLLcSFeZBP1F33/RkPM0i6AnoxKFakHvJ8G6yypooT"
                    + "9ZH438YfarRHAQf+AbPhkFuNZJAuw2lS//+XGHFY9E+YCcz0sWJ6JJg8pfvr5Aa9mGbQIDAQAB");
    private static final KeyWithAlgorithm rsaPrivKey =
            KeyWithAlgorithm.fromString("RSA:MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDN+p4EwZeMdOq"
                    + "s5z4TDWcRaMt03EEEq0deHlaYUO2HpmtR5DzfZrTuTbMMLueGgV8hwj5cnXhZ+n3eMG86jZ2LGtYWmbyo4NwT"
                    + "PjTtjz27v8vkLzuwYHsWxq6jbCp66leOhjYFYefpn0mA224S4VpBHSNOTEl3z6Wg5FAaIF9T7VRnT/xZYt2KF"
                    + "NWelgZBngzWSE6B1g9nWIorxWHrCygCpKOTGgfKoVYqQhT+pKukstxIV5kE/UXff9GQ8zSLoCejEoVqQe8nwb"
                    + "rLKmihP1kfjfxh9qtEcBB/4Bs+GQW41kkC7DaVL//5cYcVj0T5gJzPSxYnokmDyl++vkBr2YZtAgMBAAECggE"
                    + "AEer8NgO1MDW3eGUBRF0FG0GXeUnzqflQUwKmm8dmckdqzIvjM7fWg2hk6+lkoJG+ecxQ6nOUVZdxvZNPCbPq"
                    + "AYDLINoszDALVO0zY3rzbtKnZOkq8xPhgUC1TmgJZfnetfo81skGiI8fsMLl12SdGk7zlEsUlQSOLunNgghQ4"
                    + "pb5dpMfhyp0Q4ThmlfCBhY/XsRm9KLF98Il94QO9orYCJnVjOos/lWd6UKuLWEOf3CL/ucIaUAkUmu8PMO/AH"
                    + "X9xW6vNIr76rvdasocUjv3KpFtV5gQX3IhKhehuQlW758a/EeNL725QhjfesF7tKPtsSPWzQ8dyFjHWF6xn21"
                    + "4jQKBgQD+1Zj7yHF57/nfHEvXRyhkbaDkqU/uGNFUTGg0TvucAS9sas8CjJ7WHBrUfjfWWxrCNqAY2sfpxlUd"
                    + "+0di3aWrUwyM1h91dYAhYk5NHnzkjhSi4wcbwHjN+BRPRMjgp+BsF/ZySpZK/tHUbCUgyQHWtJvkpHdiHcTDZ"
                    + "h5wII9/4wKBgQDO68/I6qmoTpHRxF9zpOePTnwjWBwJ7r3qlTnFoQJGNEusDglI2GiaD3lRSxF1TfKnivUYbE"
                    + "hrHbMXbfn2lOwPlHtpjESVAseWU6Qmz6r/TITk4M8kIEzo+yomM6QeBJwd4JAgjot456sT5X+Vv0NCtfweB0e"
                    + "x2geZsK4X9MERbwKBgGlVPsvr+UOutrjLCGouhnqkedmqRlijN3tBrdzZPNUqBEErEO/70fesXEay+T+IHtJi"
                    + "I+DCJdnyWeJvp/0sorrjNA/OvegeLl0eNkFYNcV/GPaPIrQM5aI1RafSRbneijwD16E8RU0wcOj93objrvfhZ"
                    + "YKnnJUYuukNf81XGBmDAoGAXDJj/eDZQV3oyS+XXD7A0nClDVaH/8D5rBlbiXxJOCC7CumiJ2wNh3+XjapGGB"
                    + "9oHFDlDkHJLrkoACuHceA/Il4Fcy0FreN0LL4N6SEkzuY4XIbypOUjf7fRuv3NhXaGXSWe8nKxIGkRKCdc5ss"
                    + "22/WcZYDW6B7+vfMkTxZGJE8CgYEAv67Q70wtHRsl/3tnVUTgzBeB9HipilEinkkCUkDqYEf3pH6dhlmtkPi9"
                    + "YHvV38VH7AT6zqiI86mlPE7iQKEkBrYajrGEQ0UrqkjebVyN3wTwtKBXfhDkg4f2E58tcQrsaiGfMYG2/F8/B"
                    + "IRhPpqFUQzq03mgmFZtAqyhXl62o2w=");

    @Test
    public void weCanConstructFromAValidAesString() {
        String valid = "enc:QjR4AHIYoIzvjEHf53XETM3QYnCl1mgFYC51Q7x4ebwM+h3PHVqSt/"
                + "1un/+KvpJ2mZfMH0tifu+htRVxEPyXmt88lyKB83NpesNJEoLFLL+wBWCkppaLRuc/1w==";

        EncryptedValue encryptedValue = EncryptedValue.of(valid);
        assertThat(encryptedValue.getDecryptedValue(aesKey), is(plaintext));
    }

    @Test
    public void weCanConstructFromAValidRsaString() {
        String valid = "enc:GNOe/P/KQ8fvuhhBVNMZQ2jDu+cdv7im1N4GamZ64u9LhvoiLP6RiS"
                + "FnHFRcbIupEIxJQ1IM/9cJ0DpUsxPpObH+vV0fCZZ/Aqrb08s46hodTPDLU76JN"
                + "rtaxlCssXYxFN/Ni8k95pKauwPxRfvTP0SUf7o9rsZrY6LdV9+M3y6mNrEIKevA"
                + "ZQZtNmvXriclQGV1CwRzV/0sNVuTfNqNw0lDsI4hcvC26DhLrXla8jCUiKEYDFA"
                + "qVr2DaTwtV3htxtCB36Jk6Lg5abdcc9B/ZqV7lfUIddGEuXFzhz8KIIGtwVVXqi"
                + "s15Dw1ECSNJhicHZp43vSYN9y9NJTnvTAhCQ==";

        EncryptedValue encryptedValue = EncryptedValue.of(valid);
        assertThat(encryptedValue.getDecryptedValue(rsaPrivKey), is(plaintext));
    }

    @Test
    public void weCanDecryptValueEncryptedUsingExistingRsaKey() {
        EncryptedValue encryptedValue = new RsaAlgorithm().getEncryptedValue(plaintext, rsaPubKey);
        assertThat(encryptedValue.getDecryptedValue(rsaPrivKey), is(plaintext));
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

    private void weCannotDecryptWithTheWrongKey(Algorithm algorithm) throws NoSuchAlgorithmException {
        KeyPair keyPair = algorithm.generateKey();
        KeyPair otherKeyPair = algorithm.generateKey();

        EncryptedValue encryptedValue = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());

        KeyWithAlgorithm decryptionKey = otherKeyPair.privateKey().or(otherKeyPair.publicKey());
        encryptedValue.getDecryptedValue(decryptionKey); //throws
    }

    @Test(expected = RuntimeException.class)
    public void weCannotDecryptWithTheWrongAesKey() throws NoSuchAlgorithmException {
        weCannotDecryptWithTheWrongKey(new AesAlgorithm());
    }

    @Test(expected = RuntimeException.class)
    public void weCannotDecryptWithTheWrongRsaKey() throws NoSuchAlgorithmException {
        weCannotDecryptWithTheWrongKey(new RsaAlgorithm());
    }

    private void weCanDecryptAValue(Algorithm algorithm) {
        KeyPair keyPair = algorithm.generateKey();
        EncryptedValue encryptedValue = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());

        KeyWithAlgorithm decryptionKey = keyPair.privateKey().or(keyPair.publicKey());
        String decryptedValue = encryptedValue.getDecryptedValue(decryptionKey);

        assertThat(decryptedValue, is(plaintext));
    }

    private void weCanDecryptUsingAKeyFile(Algorithm algorithm) throws IOException {
        KeyPair keyPair = algorithm.generateKey();

        Path tempDirectory = Files.createTempDirectory("keys");
        Path testKeyPath = tempDirectory.resolve("test");
        keyPair.toFile(testKeyPath);
        System.setProperty(KeyPair.KEY_PATH_PROPERTY, testKeyPath.toString());

        EncryptedValue encryptedValue = algorithm.getEncryptedValue(plaintext, keyPair.publicKey());
        String decryptedValue = encryptedValue.getDecryptedValue();

        assertThat(decryptedValue, is(plaintext));
    }

    @Test
    public void weCanDecryptAnAesValue() throws IOException {
        weCanDecryptAValue(new AesAlgorithm());
        weCanDecryptUsingAKeyFile(new AesAlgorithm());
    }

    @Test
    public void weCanDecryptAnRsaValue() throws IOException {
        weCanDecryptAValue(new RsaAlgorithm());
        weCanDecryptUsingAKeyFile(new RsaAlgorithm());
    }

}
