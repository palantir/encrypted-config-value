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

import com.google.common.collect.Multimap
import org.gradle.api.tasks.Exec

class RunTask extends Exec {

    /**
     * Configures the task to run the Docker environment and execute a Gradle task within it. Executes the 'docker run'
     * command and executes './gradlew' for the specified task in the current project. Loads the project root directory
     * as 'workspace' in the Docker container and loads the Docker and Maven cache directories from the user's home
     * directory into the container as well. The Gradle cache Docker container is loaded as the Gradle cache directory.
     */
    public void configure(String taskName,
                          String containerName,
                          Multimap<String, String> customArguments) {
        workingDir(project.rootDir)

        String projectGradleDataVolume = DockerTestRunnerPlugin.getGradleDockerDataVolumeName(project)
        String homeDir = System.getProperty('user.home')

        List<Object> arguments = []
        arguments << 'docker' << 'run'

        // add custom arguments
        customArguments.asMap().each({ key, values ->
            values.each({ value ->
                arguments << key << value
            })
        })

        arguments << '--rm'
        arguments << '-w' << '/workspace'
        arguments << '-v' << "${project.rootDir.absolutePath}:/workspace"
        arguments << '-v' << "${homeDir}/.docker:/root/.docker"
        arguments << '-v' << "${homeDir}/.m2:/root/.m2"
        arguments << '-v' << "${projectGradleDataVolume}:/root/.gradle"
        arguments << '--name' << DockerTestRunnerPlugin.getContainerRunName(project, containerName)
        arguments << containerName
        arguments << '/bin/bash' << '-c'
        arguments << "./gradlew --stacktrace ${project.path}:${taskName}"

        commandLine(arguments)
    }

}
