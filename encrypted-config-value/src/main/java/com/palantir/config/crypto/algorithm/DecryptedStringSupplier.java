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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@FunctionalInterface
public interface DecryptedStringSupplier {
    String get() throws
        BadPaddingException,
        IllegalBlockSizeException,
        InvalidAlgorithmParameterException,
        InvalidKeyException,
        InvalidKeySpecException,
        NoSuchAlgorithmException,
        NoSuchPaddingException,
        NoSuchProviderException;

    static String silently(DecryptedStringSupplier supplier) {
        try {
            return supplier.get();
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            throw new RuntimeException("the key was invalid", e);
        } catch (NoSuchPaddingException | BadPaddingException e) {
            throw new RuntimeException("the padding was invalid", e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("illegal block size", e);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new RuntimeException("there was not a provider for the given algorithm", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("the algorithm parameter was invalid", e);
        }
    }
}
