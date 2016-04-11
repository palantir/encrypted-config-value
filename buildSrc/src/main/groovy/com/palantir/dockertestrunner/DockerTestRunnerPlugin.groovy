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

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testing.jacoco.plugins.JacocoPlugin

class DockerTestRunnerPlugin implements Plugin<Project> {

    private static final String GROUP_NAME = 'Docker Test Runner'
    private static final String TASK_STRING = 'DockerTestRunner'

    static String getGradleDockerDataVolumeName(Project project) {
        return "${NameUtils.sanitizeForDocker(project.rootProject.name)}-gradle-data"
    }

    static String getContainerRunName(Project project, String containerName) {
        return "${project.name}-${NameUtils.sanitizeForDocker(containerName)}"
    }

    void apply(Project project) {
        DockerTestRunnerExtension ext = project.extensions.create('dockerTestRunner', DockerTestRunnerExtension)

        // tasks are added after evaluation because they are determined by the "dockerFiles" value in the extension
        // object, which is not available until after initial evaluation.
        project.afterEvaluate {
            List<Task> buildDockerTasks = []
            List<Task> testDockerTasks = []
            List<Task> jacocoDockerTasks = []

            // each Dockerfile gets its own set of tasks
            Map<String, DockerRunner> dockerRunners = ext.getDockerRunners()
            dockerRunners.each { containerName, dockerRunner ->
                String currGroupName = getGroupName(containerName)

                // task that builds the image
                BuildTask buildTask = project.tasks.create(getBuildTaskName(containerName), BuildTask, {
                    group = currGroupName
                    description = "Build the Docker test environment image ${containerName}."
                })
                buildTask.configure(containerName, dockerRunner.dockerFile)
                buildDockerTasks << buildTask

                // task that runs the tests for this project in the container
                RunTask runTestTask = project.tasks.create("runTest${TASK_STRING}-${containerName}", RunTask, {
                    group = currGroupName
                    description = "Run tests in the Docker test environment container ${containerName}."
                })

                // create runner args -- combination of over-all args and runner-specific args
                Multimap<String, String> runnerArgs = ArrayListMultimap.create(ext.runArgs)
                runnerArgs.putAll(dockerRunner.runArgs)

                runTestTask.configure(getTestTaskName(containerName), containerName, runnerArgs)
                runTestTask.dependsOn(buildTask)
                testDockerTasks << runTestTask

                // test task that should be run in container. Is a standard "Test" task whose output parameters are
                // configured to write output to directory based on container name.
                TestTask testTask = project.tasks.create(getTestTaskName(containerName), TestTask)
                testTask.configure(containerName, [ext.testConfig, dockerRunner.testConfig])

                // add Jacoco plugins only if Jacoco plugin has been applied
                project.plugins.withType(JacocoPlugin) {
                    // task that creates the Jacoco coverage report for this project in the container (will run the tests
                    // in the container if needed).
                    RunTask runJacocoReportTask = project.tasks.create("runJacocoTestReport${TASK_STRING}-${containerName}", RunTask, {
                        group = currGroupName
                        description = "Generate Jacoco coverage report in the Docker test environment container ${containerName}."
                    })
                    runJacocoReportTask.configure(getJacocoTaskName(containerName), containerName, runnerArgs)
                    runJacocoReportTask.dependsOn(buildTask)
                    jacocoDockerTasks << runJacocoReportTask

                    // report task that should be run in container. Is a standard "JacocoReport" task whose input and output
                    // parameters are configured to read input and write output to directories based on container name.
                    // Only added if Jacoco plugin is applied.
                    JacocoReportTask jacocoReportTask = project.tasks.create(getJacocoTaskName(containerName), JacocoReportTask)
                    jacocoReportTask.configure(containerName, [ext.jacocoConfig, dockerRunner.jacocoConfig])
                    jacocoReportTask.dependsOn(testTask)
                }
            }

            if (!dockerRunners.isEmpty()) {
                // add tasks that will perform the individual tasks for all Dockerfiles
                String allGroupName = getGroupName('All')
                project.task("build${TASK_STRING}", {
                    group = allGroupName
                    description = 'Build all of the Docker test environment containers.'
                }).setDependsOn(buildDockerTasks)
                setTaskOrdering(buildDockerTasks)

                project.task("test${TASK_STRING}", {
                    group = allGroupName
                    description = 'Run tests in all of the Docker test environment containers.'
                }).setDependsOn(testDockerTasks)
                setTaskOrdering(testDockerTasks)

                if (!jacocoDockerTasks.isEmpty()) {
                    project.task("jacocoTestReport${TASK_STRING}", {
                        group = allGroupName
                        description = 'Generate Jacoco coverage reports in all of the Docker test environment containers.'
                    }).setDependsOn(jacocoDockerTasks)
                    setTaskOrdering(jacocoDockerTasks)
                }

                CreateGradleCacheVolumeTask createCacheVolumeTask = project.tasks.create('createGradleCacheVolume', CreateGradleCacheVolumeTask) {
                    group = allGroupName
                    description = 'Create Gradle cache volume and copy current cache content into it.'
                }
                String createVolumeContainer = ext.createGradleCacheVolumeImage
                if (createVolumeContainer == null) {
                    createVolumeContainer = dockerRunners.keySet().first()
                }
                createCacheVolumeTask.configure(createVolumeContainer)

                RemoveGradleCacheVolumeTask removeCacheVolumeTask = project.tasks.create('removeGradleCacheVolume', RemoveGradleCacheVolumeTask) {
                    group = allGroupName
                    description = 'Remove Gradle cache volume.'
                }
                removeCacheVolumeTask.configure()
            }
        }
    }

    /**
     * Sets the 'shouldRunAfter' property on the provided tasks such that they are specified to run in the provided
     * order. Specifically, each task's 'shouldRunAfter' is set to be the task that immediately precedes it.
     */
    private static void setTaskOrdering(List<Task> tasks) {
        for (int i = tasks.size() - 1; i > 0; i--) {
            tasks.get(i).shouldRunAfter(tasks.get(i-1))
        }
    }

    private static String getGroupName(String subGroup) {
        return "${GROUP_NAME}: ${subGroup}"
    }

    private static String getBuildTaskName(String containerName) {
        return "build${TASK_STRING}-${containerName}"
    }

    private static String getTestTaskName(String containerName) {
        return "test${TASK_STRING}-${containerName}"
    }

    private static String getJacocoTaskName(String containerName) {
        return "jacocoTestReport${TASK_STRING}-${containerName}"
    }

}
