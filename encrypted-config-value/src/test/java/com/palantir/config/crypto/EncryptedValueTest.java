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

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.config.crypto.algorithm.Algorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class EncryptedValueTest {
    private static final String plaintext = "my secret. I don't want anyone to know this";
    private static final KeyWithType aesKey = KeyWithType.fromString("AES:rqrvWpLld+wKLOyxJYxQVg==");
    private static final KeyWithType rsaPubKey = KeyWithType.fromString(
            "RSA:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzfqeBMGXjHTqrOc+Ew1nEWjLdNxBBKtHXh5WmFDth6ZrUeQ"
                    + "832a07k2zDC7nhoFfIcI+XJ14Wfp93jBvOo2dixrWFpm8qODcEz407Y89u7/L5C87sGB7Fsauo2wqeupXjoY2"
                    + "BWHn6Z9JgNtuEuFaQR0jTkxJd8+loORQGiBfU+1UZ0/8WWLdihTVnpYGQZ4M1khOgdYPZ1iKK8Vh6wsoAqSjk"
                    + "xoHyqFWKkIU/qSrpLLcSFeZBP1F33/RkPM0i6AnoxKFakHvJ8G6yypooT9ZH438YfarRHAQf+AbPhkFuNZJAu"
                    + "w2lS//+XGHFY9E+YCcz0sWJ6JJg8pfvr5Aa9mGbQIDAQAB");
    private static final KeyWithType rsaPrivKey = KeyWithType.fromString(
            "RSA:MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDN+p4EwZeMdOqs5z4TDWcRaMt03EEEq0deHlaYUO2"
                    + "HpmtR5DzfZrTuTbMMLueGgV8hwj5cnXhZ+n3eMG86jZ2LGtYWmbyo4NwTPjTtjz27v8vkLzuwYHsWxq6jbCp6"
                    + "6leOhjYFYefpn0mA224S4VpBHSNOTEl3z6Wg5FAaIF9T7VRnT/xZYt2KFNWelgZBngzWSE6B1g9nWIorxWHrC"
                    + "ygCpKOTGgfKoVYqQhT+pKukstxIV5kE/UXff9GQ8zSLoCejEoVqQe8nwbrLKmihP1kfjfxh9qtEcBB/4Bs+GQ"
                    + "W41kkC7DaVL//5cYcVj0T5gJzPSxYnokmDyl++vkBr2YZtAgMBAAECggEAEer8NgO1MDW3eGUBRF0FG0GXeUn"
                    + "zqflQUwKmm8dmckdqzIvjM7fWg2hk6+lkoJG+ecxQ6nOUVZdxvZNPCbPqAYDLINoszDALVO0zY3rzbtKnZOkq"
                    + "8xPhgUC1TmgJZfnetfo81skGiI8fsMLl12SdGk7zlEsUlQSOLunNgghQ4pb5dpMfhyp0Q4ThmlfCBhY/XsRm9"
                    + "KLF98Il94QO9orYCJnVjOos/lWd6UKuLWEOf3CL/ucIaUAkUmu8PMO/AHX9xW6vNIr76rvdasocUjv3KpFtV5"
                    + "gQX3IhKhehuQlW758a/EeNL725QhjfesF7tKPtsSPWzQ8dyFjHWF6xn214jQKBgQD+1Zj7yHF57/nfHEvXRyh"
                    + "kbaDkqU/uGNFUTGg0TvucAS9sas8CjJ7WHBrUfjfWWxrCNqAY2sfpxlUd+0di3aWrUwyM1h91dYAhYk5NHnzk"
                    + "jhSi4wcbwHjN+BRPRMjgp+BsF/ZySpZK/tHUbCUgyQHWtJvkpHdiHcTDZh5wII9/4wKBgQDO68/I6qmoTpHRx"
                    + "F9zpOePTnwjWBwJ7r3qlTnFoQJGNEusDglI2GiaD3lRSxF1TfKnivUYbEhrHbMXbfn2lOwPlHtpjESVAseWU6"
                    + "Qmz6r/TITk4M8kIEzo+yomM6QeBJwd4JAgjot456sT5X+Vv0NCtfweB0ex2geZsK4X9MERbwKBgGlVPsvr+UO"
                    + "utrjLCGouhnqkedmqRlijN3tBrdzZPNUqBEErEO/70fesXEay+T+IHtJiI+DCJdnyWeJvp/0sorrjNA/Ovege"
                    + "Ll0eNkFYNcV/GPaPIrQM5aI1RafSRbneijwD16E8RU0wcOj93objrvfhZYKnnJUYuukNf81XGBmDAoGAXDJj/"
                    + "eDZQV3oyS+XXD7A0nClDVaH/8D5rBlbiXxJOCC7CumiJ2wNh3+XjapGGB9oHFDlDkHJLrkoACuHceA/Il4Fcy"
                    + "0FreN0LL4N6SEkzuY4XIbypOUjf7fRuv3NhXaGXSWe8nKxIGkRKCdc5ss22/WcZYDW6B7+vfMkTxZGJE8CgYE"
                    + "Av67Q70wtHRsl/3tnVUTgzBeB9HipilEinkkCUkDqYEf3pH6dhlmtkPi9YHvV38VH7AT6zqiI86mlPE7iQKEk"
                    + "BrYajrGEQ0UrqkjebVyN3wTwtKBXfhDkg4f2E58tcQrsaiGfMYG2/F8/BIRhPpqFUQzq03mgmFZtAqyhXl62o"
                    + "2w=");

    @Test
    public void weCanConstructFromAValidLegacyAesString() {
        String valid = "enc:QjR4AHIYoIzvjEHf53XETM3QYnCl1mgFYC51Q7x4ebwM+h3PHVqSt/"
                + "1un/+KvpJ2mZfMH0tifu+htRVxEPyXmt88lyKB83NpesNJEoLFLL+wBWCkppaLRuc/1w==";

        EncryptedValue encryptedValue = EncryptedValue.fromString(valid);
        assertThat(encryptedValue.decrypt(aesKey)).isEqualTo(plaintext);
    }

    @Test
    public void weCanConstructFromAValidString() {
        String valid = "enc:eyJ0eXBlIjoiQUVTIiwibW9kZSI6IkdDTSIsIml2IjoiazhKTEJpVV"
                + "hHWmYyQk1aUSIsImNpcGhlcnRleHQiOiI1U1R3Z0hTaHB0Q1ErNCtmT0lMRC8xRHM5R3pp"
                + "K1RXSkZwckpHWGdUVXVRRmx4Nnd0a0lwNVFUcE1RPT0iLCJ0YWciOiJZTUNURlY2b2dsemxwV3FOVlp3YVp3PT0ifQ==";

        EncryptedValue encryptedValue = EncryptedValue.fromString(valid);
        assertThat(encryptedValue.decrypt(aesKey)).isEqualTo(plaintext);
    }

    @Test
    public void weCanConstructFromAValidRsaString() throws Exception {
        String valid = "enc:GNOe/P/KQ8fvuhhBVNMZQ2jDu+cdv7im1N4GamZ64u9LhvoiLP6RiS"
                + "FnHFRcbIupEIxJQ1IM/9cJ0DpUsxPpObH+vV0fCZZ/Aqrb08s46hodTPDLU76JN"
                + "rtaxlCssXYxFN/Ni8k95pKauwPxRfvTP0SUf7o9rsZrY6LdV9+M3y6mNrEIKevA"
                + "ZQZtNmvXriclQGV1CwRzV/0sNVuTfNqNw0lDsI4hcvC26DhLrXla8jCUiKEYDFA"
                + "qVr2DaTwtV3htxtCB36Jk6Lg5abdcc9B/ZqV7lfUIddGEuXFzhz8KIIGtwVVXqi"
                + "s15Dw1ECSNJhicHZp43vSYN9y9NJTnvTAhCQ==";

        EncryptedValue encryptedValue = EncryptedValue.fromString(valid);
        assertThat(encryptedValue.decrypt(rsaPrivKey)).isEqualTo(plaintext);
    }

    @Test
    public void weCanDecryptValueEncryptedUsingExistingRsaKey() {
        EncryptedValue encryptedValue = Algorithm.RSA.newEncrypter().encrypt(rsaPubKey, plaintext);
        assertThat(encryptedValue.decrypt(rsaPrivKey)).isEqualTo(plaintext);
    }

    @Test
    public void weFailToConstructWithInvalidPrefix() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "anc:TCkE/OT7xsKWqP4SRNBEj54Pk7wDMQzMGJtX90toFuGeejM/LQBDTZ8hEaKQt/3i";
            EncryptedValue.fromString(invalid);
        }); // throws
    }

    @Test
    public void weFailToConstructWithAnInvalidEncryptedValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String invalid = "enc:verysecret^^";
            EncryptedValue.fromString(invalid);
        }); // throws if suffix is not a base64-encoded string
    }

    private void weCannotDecryptWithTheWrongKey(Algorithm algorithm) throws NoSuchAlgorithmException {
        KeyPair keyPair = algorithm.newKeyPair();
        KeyPair otherKeyPair = algorithm.newKeyPair();

        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        KeyWithType decryptionKey = otherKeyPair.decryptionKey();
        encryptedValue.decrypt(decryptionKey); // throws
    }

    @Test
    public void weCannotDecryptWithTheWrongAesKey() throws NoSuchAlgorithmException {
        Assertions.assertThrows(RuntimeException.class, () -> {
            weCannotDecryptWithTheWrongKey(Algorithm.AES);
        });
    }

    @Test
    public void weCannotDecryptWithTheWrongRsaKey() throws NoSuchAlgorithmException {
        Assertions.assertThrows(RuntimeException.class, () -> {
            weCannotDecryptWithTheWrongKey(Algorithm.RSA);
        });
    }

    private void weCanDecryptAValue(Algorithm algorithm) {
        KeyPair keyPair = algorithm.newKeyPair();
        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);

        KeyWithType decryptionKey = keyPair.decryptionKey();
        String decryptedValue = encryptedValue.decrypt(decryptionKey);

        assertThat(decryptedValue).isEqualTo(plaintext);
    }

    private void weCanDecryptUsingAKeyFile(Algorithm algorithm) throws IOException {
        KeyPair keyPair = algorithm.newKeyPair();

        Path tempDirectory = Files.createTempDirectory("keys");
        Path testKeyPath = tempDirectory.resolve("test");
        KeyFileUtils.keyPairToFile(keyPair, testKeyPath);
        System.setProperty(KeyFileUtils.KEY_PATH_PROPERTY, testKeyPath.toString());

        EncryptedValue encryptedValue = algorithm.newEncrypter().encrypt(keyPair.encryptionKey(), plaintext);
        String decryptedValue = encryptedValue.decrypt(keyPair.decryptionKey());

        assertThat(decryptedValue).isEqualTo(plaintext);
    }

    @Test
    public void weCanDecryptAnAesValue() throws IOException {
        weCanDecryptAValue(Algorithm.AES);
        weCanDecryptUsingAKeyFile(Algorithm.AES);
    }

    @Test
    public void weCanDecryptAnRsaValue() throws IOException {
        weCanDecryptAValue(Algorithm.RSA);
        weCanDecryptUsingAKeyFile(Algorithm.RSA);
    }
}
