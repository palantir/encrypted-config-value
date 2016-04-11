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

import org.gradle.api.file.FileCollection
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoReportTask extends JacocoReport {

    /**
     * Configures the task to run all of the tests for the current project. Modifies the JUnit XML and HTML test report
     * destinations of the default test configuration so that they are concatenated with the name of the container in
     * which the tests are meant to be run. Runs all of the tests in the "test" source set of the current project using
     * the "test" runtime classpath of the current project.
     */
    public void configure(String containerName, List<Closure> config) {
        String sanitizedName = NameUtils.sanitizeForPath(containerName)
        reports.xml.enabled = true
        reports.xml.destination("${project.jacoco.getReportsDir().absolutePath}/${sanitizedName}/jacocoTestReport.xml")
        reports.html.destination("${project.jacoco.getReportsDir().absolutePath}/${sanitizedName}/html")

        executionData = project.files(TestTask.getJacocoDestinationFile(project, containerName)).getAsFileTree()
        sourceDirectories = project.files(project.sourceSets.main.allSource.srcDirs)
        classDirectories = project.files(project.sourceSets.main.output.classesDir)

        // invoke all non-null closures that were provided
        config.each {
            if (it) {
                it.delegate = this
                it()
            }
        }
    }

}
