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
package com.palantir.config.crypto.algorithm;

import static com.google.common.base.Preconditions.checkArgument;

import com.palantir.config.crypto.EncryptedValue;
import com.palantir.config.crypto.KeyWithAlgorithm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AesAlgorithm implements Algorithm {

    public static final String ALGORITHM_TYPE = "AES";

    private static final int GCM_AUTH_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 256 / Byte.SIZE;
    private static final Charset charset = StandardCharsets.UTF_8;

    private Cipher getUninitializedCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES/GCM/PKCS5Padding");
    }

    @Override
    public EncryptedValue getEncryptedValue(String plaintext, KeyWithAlgorithm kwa) {
        checkArgument(kwa.getAlgorithm().equals(ALGORITHM_TYPE),
                "key must be for AES algorithm but was " + kwa.getAlgorithm());

        try {
            Cipher cipher = getUninitializedCipher();
            Key secretKeySpec = getSecretKeySpec(kwa);

            byte[] ivBytes = new byte[IV_LENGTH];

            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivBytes);

            GCMParameterSpec gcmSpecWithIv = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH, ivBytes);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpecWithIv);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(charset));

            // put together the iv and the encrypted bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(ivBytes);
            outputStream.write(encrypted);

            String encryptedString =  Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return EncryptedValue.fromEncryptedString(encryptedString);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("The cipher could not be initialized", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The supplied key was not valid", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("The encrypted value was not valid", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("The iv parameter was bad", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not concat byte arrays for iv and encrypted bytes", e);
        }
    }

    @Override
    public String getDecryptedString(EncryptedValue encryptedValue, KeyWithAlgorithm kwa) {
        checkArgument(kwa.getAlgorithm().equals(ALGORITHM_TYPE),
                "key must be for AES algorithm but was " + kwa.getAlgorithm());

        try {
            Cipher cipher = getUninitializedCipher();
            Key secretKeySpec = getSecretKeySpec(kwa);

            String ciphertext = encryptedValue.encryptedValue();
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);

            GCMParameterSpec gcmSpecWithIv = new GCMParameterSpec(GCM_AUTH_TAG_LENGTH, cipherBytes, 0, IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpecWithIv);

            byte[] decrypted = cipher.doFinal(cipherBytes, IV_LENGTH, cipherBytes.length - IV_LENGTH);
            return new String(decrypted, charset);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("The cipher could not be initialized", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The supplied key was not valid", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("The encrypted value was not valid", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("The iv parameter was bad", e);
        }
    }

    @Override
    public KeyWithAlgorithm generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_TYPE);
            keyGen.init(128); // TODO: should we try for more if we have the proper JCE?
            SecretKey secretKey = keyGen.generateKey();
            return KeyWithAlgorithm.from(ALGORITHM_TYPE, secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No AES algorithm", e);
        }
    }

    private static SecretKeySpec getSecretKeySpec(KeyWithAlgorithm key) {
        return new SecretKeySpec(key.getKey(), key.getAlgorithm());
    }

}
