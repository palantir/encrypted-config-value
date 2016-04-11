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

import com.google.common.collect.ImmutableMap
import org.gradle.api.file.FileCollection

class DockerTestRunnerExtension implements DockerRunnerConfig {

    // stores the runners. Uses a {@link LinkedHashMap} to maintain items in insertion order.
    private final Map<String, DockerRunner> dockerRunners = new LinkedHashMap<>()

    /**
     * Adds the provided DockerRunner with the provided name and configuration.
     */
    void dockerRunner(Map params) {
        // file must be specified
        File dockerFile = params['dockerFile']
        if (!dockerFile) {
            throw new IllegalArgumentException('dockerFile was not specified')
        }

        String name = params['name']
        if (name) {
            name = NameUtils.sanitizeForDocker(name)
        } else {
            // if name is not specified, generate based on file
            name = getDockerNameForFile(dockerFile)
        }

        DockerRunner runner = new DockerRunner(dockerFile: dockerFile, testConfig: params['testConfig'], jacocoConfig: params['jacocoConfig'])
        dockerRunners << [(name): runner]
    }

    /**
     * Adds the provided files as Docker runners with generated names based on their file path. All of the files must
     * exist and must be regular files (they cannot be directories). The keys in the returned map are Strings of the
     * form "parent/filename" and are sanitized for Docker use -- the "parent" and "filename" portion are all lowercase
     * and any unsupported character is replaced with an underscore ('_'). If the provided files do not have unique
     * names after the transformation is applied, an exception is thrown.
     */
    void dockerFiles(FileCollection files) {
        Collection unsupportedFiles = files.findAll { file -> !file.exists() || file.isDirectory() }
        if (!unsupportedFiles.isEmpty()) {
            throw new IllegalStateException("The following files were either nonexistent or were directories: ${unsupportedFiles}")
        }

        Map<String, List<File>> groupedByName = files.groupBy { file -> DockerTestRunnerExtension.getDockerNameForFile(file) }

        Collection filesWithNameCollisions = groupedByName.findAll { it.value.size() > 1 }.collect { it.value }
        if (!filesWithNameCollisions.isEmpty()) {
            throw new IllegalStateException("Multiple files had the \"parent/file\" name after being standardized: ${filesWithNameCollisions}");
        }

        dockerRunners.putAll((Map<String, DockerRunner>) groupedByName.collectEntries {
            [(it.key): new DockerRunner(dockerFile: it.value.get(0))]
        })
    }

    /**
     * Returns the Docker runners.
     */
    Map<String, DockerRunner> getDockerRunners() {
        return ImmutableMap.copyOf(dockerRunners)
    }

    /**
     * The image to use for the 'CreateGradleCacheVolumeTask' task. Can be the name of an image provided as a Docker
     * runner or the name of any image that can be provided and resolved by 'docker run'. If null, then the image used
     * by the first docker runner will be used.
     */
    String createGradleCacheVolumeImage

    private static String getDockerNameForFile(File file) {
        return "${NameUtils.sanitizeForDocker(file.parentFile.name)}/${NameUtils.sanitizeForDocker(file.name)}"
    }

}
