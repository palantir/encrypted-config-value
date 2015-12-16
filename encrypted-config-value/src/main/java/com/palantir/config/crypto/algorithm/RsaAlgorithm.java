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
import com.palantir.config.crypto.KeyPair;
import com.palantir.config.crypto.KeyWithAlgorithm;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public final class RsaAlgorithm implements Algorithm {

    public static final String ALGORITHM_TYPE = "RSA";
    private static final Charset charset = StandardCharsets.UTF_8;

    private Cipher getUninitializedCipher() throws NoSuchAlgorithmException,
        NoSuchPaddingException, NoSuchProviderException {
        return Cipher.getInstance(ALGORITHM_TYPE);
    }

    @Override
    public EncryptedValue getEncryptedValue(String plaintext, KeyWithAlgorithm kwa) {
        checkArgument(kwa.getAlgorithm().equals(ALGORITHM_TYPE),
                "key must be for RSA algorithm but was %s", kwa.getAlgorithm());

        return EncryptedValueSupplier.silently(() -> {
            Cipher cipher = getUninitializedCipher();
            PublicKey publicKey = generatePublicKey(kwa);

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(charset));

            String encryptedString =  Base64.getEncoder().encodeToString(encrypted);
            return EncryptedValue.fromEncryptedString(encryptedString);
        });
    }

    @Override
    public String getDecryptedString(EncryptedValue encryptedValue, KeyWithAlgorithm kwa) {
        checkArgument(kwa.getAlgorithm().equals(ALGORITHM_TYPE),
                "key must be for RSA algorithm but was %s", kwa.getAlgorithm());

        return DecryptedStringSupplier.silently(() -> {
            Cipher cipher = getUninitializedCipher();
            PrivateKey privateKey = generatePrivateKey(kwa);

            String ciphertext = encryptedValue.encryptedValue();
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, charset);
        });
    }

    // produce a key using up to a 2048 bit length
    private int getKeyLength() throws NoSuchAlgorithmException {
        return Math.min(Cipher.getMaxAllowedKeyLength(ALGORITHM_TYPE), 2048);
    }

    @Override
    public KeyPair generateKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_TYPE);
            keyPairGenerator.initialize(getKeyLength());

            java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
            KeyWithAlgorithm publicKey = KeyWithAlgorithm.from(ALGORITHM_TYPE, keyPair.getPublic().getEncoded());
            KeyWithAlgorithm privateKey = KeyWithAlgorithm.from(ALGORITHM_TYPE, keyPair.getPrivate().getEncoded());

            return KeyPair.of(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No RSA algorithm", e);
        }
    }

    private static PublicKey generatePublicKey(KeyWithAlgorithm key)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = KeyFactory.getInstance(ALGORITHM_TYPE)
                .generatePublic(new X509EncodedKeySpec(key.getKey()));
        return publicKey;
    }

    private static PrivateKey generatePrivateKey(KeyWithAlgorithm key)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM_TYPE)
                .generatePrivate(new PKCS8EncodedKeySpec(key.getKey()));
        return privateKey;
    }

    @Override
    public String getName() {
        return ALGORITHM_TYPE;
    }
}
