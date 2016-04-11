/*
 * Copyright 2016 Palantir Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.dockertestrunner

final class NameUtils {
    private NameUtils() {
    }

    /**
     * Returns a version of the provided string that is legal to use as a Docker container identifier (A-Za-z0-9_-). All
     * characters are converted to lowercase and any characters that are not in the set of acceptable characters are
     * replaced with an underscore ('_').
     */
    static String sanitizeForDocker(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.') {
                builder.append(Character.toLowerCase(c))
            } else {
                builder.append('_')
            }
        }

        return builder.toString()
    }

    /**
     * Returns the provided String with all '/' characters replaced with '_' so that the name can be used as part
     * of a path without causing directories to be created.
     */
    static String sanitizeForPath(String name) {
        return name.replaceAll('/', '_')
    }

}
