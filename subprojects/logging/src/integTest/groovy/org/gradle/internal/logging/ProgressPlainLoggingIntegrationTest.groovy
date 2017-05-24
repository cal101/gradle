/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.logging

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class ProgressPlainLoggingIntegrationTest extends AbstractIntegrationSpec {
    def setup() {
        executer.withArgument("--console=plain")
        buildFile << """
            task foo {
                logger.quiet 'foo Logged during configuration'
                doFirst {
                    logger.quiet 'foo First line of text'
                    logger.quiet 'foo Second line of text'
                }
            }
            
            task bar(dependsOn: foo) {
                logger.quiet 'bar Logged during configuration'
                doFirst {
                    logger.quiet 'bar First line of text'
                    logger.quiet 'bar Second line of text'
                }
            }
        """
    }

    def "logs at configuration time are grouped with a header"() {
        when:
        succeeds('bar')

        then:
        result.assertOutputContains("""Configure project :
foo Logged during configuration
bar Logged during configuration""")
    }

    def "logs at execution time are grouped with a header"() {
        when:
        succeeds('bar')

        then:
        result.assertOutputContains(""":foo
foo First line of text
foo Second line of text
:bar
bar First line of text
bar Second line of text""")
    }

    def "tasks and outcomes are always logged"() {
        given:
        buildFile << "task baz {}"

        when:
        succeeds('baz')

        then:
        result.assertOutputContains(':baz UP-TO-DATE')
    }

    def "logs outside of configuration and execution are ungrouped"() {
        given:
        buildFile << """Thread.start { 
            println 'log message from left field'
            println 'log message from right field'
        } 
        
        task qux {
            logger.quiet 'qux logged during configuration'
        }
        """

        when:
        succeeds('qux')

        then:
        // Note the empty line here denotes ungrouped output
        result.assertOutputContains("""qux logged during configuration

log message from left field
log message from right field
:qux""")
    }
}
