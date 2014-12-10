/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.publish.maven

class MavenPublicationVersionRangeIntegTest extends AbstractMavenPublishIntegTest {
    def mavenModule = mavenRepo.module("org.gradle.test", "publishTest", "1.9")

    public void "can publish jar and meta-data to maven repository"() {
        given:

        mavenRepo.module('group', 'projectA', '1.0').publish()
        mavenRepo.module('group', 'projectA', '2.0').publish()
        mavenRepo.module('group', 'projectB', '1.1.1').publish()
        mavenRepo.module('group', 'projectC', '1.2').publish()

        settingsFile << "rootProject.name = 'publishTest' "
        buildFile << """
            apply plugin: 'maven-publish'
            apply plugin: 'java'

            group = 'org.gradle.test'
            version = '1.9'

            publishing {
                repositories {
                    maven { url "${mavenRepo.uri}" }
                }
                publications {
                    maven(MavenPublication) {
                        from components.java
                    }
                }
            }

            dependencies {
                compile "group:projectA:latest.release"
                compile "group:projectB:latest.integration"
                runtime "group:projectC:1.1.+"
                runtime "group:projectD:1+"
            }"""

        when:
        run "publish"

        then:
        mavenModule.assertPublishedAsJavaModule()

        mavenModule.parsedPom.scopes.keySet() == ["runtime"] as Set
        mavenModule.parsedPom.scopes.runtime.assertDependsOn("group:projectA:RELEASE", "group:projectB:LATEST", "group:projectC:[1.1,1.2)",  "group:projectD:[1,2)")
    }
}