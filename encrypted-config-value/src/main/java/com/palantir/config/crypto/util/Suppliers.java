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

import com.palantir.config.crypto.supplier.ThrowingSupplier;
import com.palantir.logsafe.exceptions.SafeRuntimeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class Suppliers {
    private Suppliers() {
        /* do not instantiate */
    }

    public static <T> T silently(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (AEADBadTagException e) {
            throw new SafeRuntimeException(
                    "couldn't verify the message's authentication tag "
                            + "- either the message was tampered with, or the key is invalid",
                    e);
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            throw new SafeRuntimeException("the key was invalid", e);
        } catch (NoSuchPaddingException | BadPaddingException e) {
            throw new SafeRuntimeException("the padding was invalid", e);
        } catch (IllegalBlockSizeException e) {
            throw new SafeRuntimeException("illegal block size", e);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new SafeRuntimeException("there was not a provider for the given algorithm", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new SafeRuntimeException("the algorithm parameter was invalid", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SafeRuntimeException(e);
        } catch (Exception e) {
            throw new SafeRuntimeException(e);
        }
    }
}
