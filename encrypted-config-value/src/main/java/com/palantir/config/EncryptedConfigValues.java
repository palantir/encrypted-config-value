/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class EncryptedConfigValues {
    private static final Charset charset = Charsets.UTF_8;

    private EncryptedConfigValues() {
        /* do not instantiate */
    }

    /**
     * Checks that this encrypted value is valid and decodes it into a byte array.
     * If the value is invalid, an {@link IllegalArgumentException} will be thrown.
     * @param encryptedValue  the encrypted value to convert and validate
     * @return this encrypted value as bytes
     */
    public static byte[] getBytesAndValidate(String encryptedValue) {
        return Base64.getDecoder().decode(encryptedValue);
    }

    /**
     * @param encryptedValue  an encrypted string representing UTF-8 encoded bytes.
     * @param key             the decryption key.
     * @return the plaintext
     */
    public static String getDecryptedValue(String encryptedValue, KeyWithAlgorithm key) {
        SecretKeySpec secretKeySpec = getSecretKeySpec(key);

        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] rawBytes = getBytesAndValidate(encryptedValue);
            byte[] decrypted = cipher.doFinal(rawBytes);
            return new String(decrypted, charset);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("The cipher could not be initialized", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The supplied key was not valid", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("The encrypted value was not valid", e);
        }
    }

    /**
     * @param plaintext  the plaintext to encrypt.
     * @param key        the encryption key to use.
     * @return a base-64 encoding of the encrypted bytes
     *
     * @see EncryptedConfigValues#getEncryptedConfigValue(String, KeyWithAlgorithm)
     */
    public static String getEncryptedString(String plaintext, KeyWithAlgorithm key) {
        SecretKeySpec secretKeySpec = getSecretKeySpec(key);

        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(charset));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("The cipher could not be initialized", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The supplied key was not valid", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("The encrypted value was not valid", e);
        }
    }

    /**
     * @param plaintext  the plaintext to encrypt.
     * @param key        the encryption key to use.
     * @return an encrypted config value for the given plaintext
     *
     * @see EncryptedConfigValues#getEncryptedString(String, KeyWithAlgorithm)
     */
    public static EncryptedConfigValue getEncryptedConfigValue(String plaintext, KeyWithAlgorithm key) {
        String encryptedString = getEncryptedString(plaintext, key);
        return EncryptedConfigValue.fromEncryptedString(encryptedString);
    }


    private static SecretKeySpec getSecretKeySpec(KeyWithAlgorithm key) {
        return new SecretKeySpec(key.getKey(), key.getAlgorithm());
    }
}
