package com.palantir.dockertestrunner

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap

trait DockerRunnerConfig {

    /**
    * Map of supplemental flags provided to the 'docker run' command. The key is the full flag including any leading
    * dashes -- for example, '-v' or '--cidfile'. The values are the values that will be provided for the key flag.
    * A separate flag will be added for each value of the key -- for example, if the key '-e' has values 'COLOR=pink'
    * and 'FOO=bar', then '-e COLOR=pink' and '-e FOO=bar' are both specified separately. The run arguments in this
    * map are specified before the built-in ones, so if there are configuration values that conflict, the built-in
    * ones will take precedence.
    */
    Multimap<String, String> runArgs = ArrayListMultimap.create()

    /**
    * Optional Closure that is applied to the {@link org.gradle.api.tasks.testing.Test} task.
    */
    Closure testConfig

    /**
    * Optional Closure that is applied on the {@link org.gradle.testing.jacoco.tasks.JacocoReport} task.
    */
    Closure jacocoConfig

}
