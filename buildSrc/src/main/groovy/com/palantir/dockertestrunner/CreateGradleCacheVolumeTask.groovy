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

import org.gradle.api.tasks.Exec

import java.util.concurrent.ConcurrentHashMap

class CreateGradleCacheVolumeTask extends Exec {

    private static final Set<String> CREATED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>())

    /**
     * Configures the task to create a Docker data volume that contains the Gradle cache files using the provided
     * container. A Gradle project will always have a single Gradle cache Docker data volume that is used by all
     * projects (including subprojects). The name for the volume is deterministic based on the name of the root project.
     * This command loads the Gradle cache Docker volume for the project (creating it if necessary) and copies the
     * contents of the Gradle cache (determined by the 'gradleUserHomeDir' of the running Gradle task) into the Docker
     * data volume. The container can be any container that supports the 'cp' operation (the container is only used to
     * create the volume and invoke the copy operation). Generally, using an image that is already part of the project
     * will be best for caching purposes, but any general image can be used.
     */
    public void configure(String containerName) {
        workingDir(project.rootDir)

        String projectGradleDataVolume = DockerTestRunnerPlugin.getGradleDockerDataVolumeName(project)

        commandLine('docker',
                    'run',
                    '--rm',
                    '-v', "${projectGradleDataVolume}:/dockerVolumeGradleData",
                    '-v', "${project.gradle.gradleUserHomeDir.absolutePath}:/hostGradleData",
                    containerName,
                    'cp', '-r', '/hostGradleData/.', '/dockerVolumeGradleData/')

        // ensure that task only runs once per Gradle execution for a particular volume
        doLast {
            CreateGradleCacheVolumeTask.CREATED.add(projectGradleDataVolume)
        }

        onlyIf {
            return !CreateGradleCacheVolumeTask.CREATED.contains(projectGradleDataVolume)
        }
    }

}
