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

class RemoveGradleCacheVolumeTask extends Exec {

    private static final Set<String> REMOVED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>())

    /**
     * Configures the task to remove the Docker data volume that contains the Gradle cache files. Uses the
     * 'docker volume rm' command, which requires Docker 1.9.0 or later. The volume must not be in use by any container.
     */
    public void configure() {
        String volumeName = DockerTestRunnerPlugin.getGradleDockerDataVolumeName(project)
        commandLine('docker',
                    'volume',
                    'rm',
                    volumeName)

        // ensure that task only runs once per Gradle execution for a particular volume
        doLast {
            RemoveGradleCacheVolumeTask.REMOVED.add(volumeName)
        }

        onlyIf {
            return !RemoveGradleCacheVolumeTask.REMOVED.contains(volumeName)
        }
    }

}
