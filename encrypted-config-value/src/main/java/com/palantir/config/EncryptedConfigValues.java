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

    public static String getDecryptedValue(String encryptedValue, KeyWithAlgorithm key) {
        SecretKeySpec secretKeySpec = getSecretKeySpec(key);

        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] rawBytes = Base64.getDecoder().decode(encryptedValue);
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

    public static String getEncryptedValue(String rawValue, KeyWithAlgorithm key) {
        SecretKeySpec secretKeySpec = getSecretKeySpec(key);

        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(rawValue.getBytes(charset));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("The cipher could not be initialized", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The supplied key was not valid", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("The encrypted value was not valid", e);
        }
    }


    private static SecretKeySpec getSecretKeySpec(KeyWithAlgorithm key) {
        return new SecretKeySpec(key.getKey(), key.getAlgorithm());
    }
}
