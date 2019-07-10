timestamps {
    node {
        properties([
            [$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator',
                artifactDaysToKeepStr: '8',
                artifactNumToKeepStr: '3',
                daysToKeepStr: '15',
                numToKeepStr: '5']
            ]]);

        final def jdks = ['OpenJDK11','JDK8']

        stage('Prepare') {
            sh "ulimit -a"
            sh "free -h"
            checkout scm
        }

        stage('Prepare data') {
            sh ".jenkins/data-prepare-topnl.sh"
        }

        jdks.eachWithIndex { jdk, indexOfJdk ->
            final String jdkTestName = jdk.toString()

            withEnv(["JAVA_HOME=${ tool jdkTestName }", "PATH+MAVEN=${tool 'Maven 3.6.1'}/bin:${env.JAVA_HOME}/bin"]) {

                echo "Using JDK: ${jdkTestName} at ${env.JAVA_HOME}"

                stage("Build: ${jdkTestName}") {
                    echo "Building branch: ${env.BRANCH_NAME}"
                    sh "mvn clean install -Dmaven.test.skip=true -B -V -e -fae -q -Poracle -pl '!brmo-dist'"
                }

                stage("Test: ${jdkTestName}") {
                    echo "Running unit tests"
                    sh "mvn -e test -B -Poracle -pl '!brmo-dist'"
                }

                lock('brmo-oracle') {
                    timeout(90) {
                        stage("Prepare Oracle Databases: ${indexOfJdk}") {
                            echo "cleanup schema's"
                            sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top10nl/top10nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top50nl/top50nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top100nl/top100nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top250nl/top250nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                        }

                        stage("Prepare Oracle staging: ${indexOfJdk}") {
                            echo "init staging schema"
                            sh ".jenkins/db-prepare-staging.sh"
                        }

                        stage("Prepare Oracle rsgb: ${indexOfJdk}") {
                            echo "init rsgb schema"
                            sh ".jenkins/db-prepare-rsgb.sh"
                        }

                        stage("Prepare Oracle rsgbbgt: ${indexOfJdk}") {
                            echo "init rsgbbgt schema"
                            sh ".jenkins/db-prepare-rsgbbgt.sh"
                        }

                        stage("Prepare Oracle topnl: ${indexOfJdk}") {
                            echo "init topnl schema"
                            sh ".jenkins/db-prepare-topnl.sh"
                        }

                        stage("bgt-gml-loader Integration Test: ${jdkTestName}") {
                            echo "run integratie tests voor bgt-gml-loader module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'bgt-gml-loader'"
                        }

                        stage("brmo-loader Integration Test: ${jdkTestName}") {
                            echo "run integratie tests voor brmo-loader module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-loader' -Dit.test=!TopNLIntegrationTest"
                        }

                        stage("brmo-service Integration Test: ${jdkTestName}") {
                            echo "run integratie tests voor brmo-service module"
                            timeout(10) {
                                try {
                                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-service'"
                                } catch (Exception e) {
                                    currentBuild.result = 'UNSTABLE'
                                }
                            }
                        }

                        stage("brmo-soap Integration Test: ${jdkTestName}") {
                            echo "run integratie tests voor brmo-soap module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-soap'"
                        }

                        stage("brmo-stufbg204 Integration Test: ${jdkTestName}") {
                            echo "run integratie tests voor brmo-stufbg204 module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-stufbg204'"
                        }

                        stage("brmo-commandline Integration Test: ${jdkTestName}") {
                            if(jdkTestName == 'JDK8') {
                                echo "run integratie tests voor brmo-commandline module"
                                sh "mvn -Djava.security.egd=file:/dev/./urandom -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-commandline'"
                            } else {
                                echo "skip integratie tests voor brmo-commandline module"
                            }
                        }

                        stage("Cleanup Database: ${indexOfJdk}") {
                            sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                            sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                            sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top10nl/top10nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top50nl/top50nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top100nl/top100nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top250nl/top250nl@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                        }

                        if(jdkTestName == 'JDK8') {
                            stage("Upgrade Database Test: ${indexOfJdk}") {
                                sh ".travis/getlastRelease.sh"
                                sh ".jenkins/setup-old.sh"
                                sh "\".jenkins/execute-upgrades-oracle.sh\" staging"
                                sh "\".jenkins/execute-upgrades-oracle.sh\" rsgb"
                                sh "\".jenkins/execute-upgrades-oracle.sh\" rsgbbgt"
                                sh "mvn -e -B -Poracle -pl 'datamodel' resources:testResources compiler:testCompile surefire:test"
                            }

                            stage("Cleanup Database 2: ${indexOfJdk}") {
                                sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                                sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                                sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
                            }
                        }
                    }
                }
            }
        }

        withEnv(["JAVA_HOME=${ tool 'JDK8' }", "PATH+MAVEN=${tool 'Maven 3.6.1'}/bin:${env.JAVA_HOME}/bin"]) {
            stage('Publish Results') {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml'
            }

            stage('Test Coverage results') {
                jacoco exclusionPattern: '**/*Test.class', execPattern: '**/target/**.exec'
            }

            stage('OWASP Dependency Check') {
                echo "Uitvoeren OWASP dependency check"
                sh "mvn org.owasp:dependency-check-maven:aggregate"

                dependencyCheckPublisher pattern: '**/dependency-check-report.xml', failedNewCritical: 1, failedNewHigh: 1, failedTotalCritical: 1, failedTotalHigh: 3, unstableTotalHigh: 2
            }

            cleanWs cleanWhenFailure: false, cleanWhenNotBuilt: false, cleanWhenUnstable: false, notFailBuild: true
        }
    }
}
