#!/usr/bin/env groovy
timestamps {
    node {
        properties([
            [$class: 'jenkins.model.BuildDiscarderProperty', strategy: 
                [$class: 'LogRotator', artifactDaysToKeepStr: '8', artifactNumToKeepStr: '3', daysToKeepStr: '15', numToKeepStr: '5']
            ]
        ]);

        stage('Prepare') {
            sh "id"
            sh "ulimit -a"
            sh "free -h"
            checkout scm
        }

        stage('Prepare data') {
            sh ".build/ci/data-prepare-topnl.sh"
        }

        withEnv([
            "JAVA_HOME=${ tool 'OpenJDK11' }",
            "PATH+MAVEN=${tool 'Maven CURRENT'}/bin:${ tool 'OpenJDK11' }/bin"
            ]) {

            echo "JAVA_HOME is ${JAVA_HOME}"
            echo "PATH is ${PATH}"

            lock('brmo-single-build') {
                stage("Build") {
                    echo "Building branch: ${env.BRANCH_NAME}"
                    sh "mvn clean install -Dmaven.test.skip=true -B -V -e -fae -q -Poracle -pl '!brmo-dist'"
                }

                stage("Test") {
                    echo "Running unit tests"
                    sh "mvn -e test -B -Poracle -pl '!brmo-dist'"
                }

                lock('brmo-oracle') {
                    // sh ".jenkins/start-oracle-brmo.sh" oracle draait al op 192.168.1.26:15210
                    timeout(180) {
                        stage("Prepare Oracle Databases") {
                            echo "cleanup schema's"
                            sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top10nl/top10nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top50nl/top50nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top100nl/top100nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top250nl/top250nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                        }

                        stage("Prepare Oracle staging") {
                            echo "init staging schema"
                            sh ".jenkins/db-prepare-staging.sh"
                        }

                        stage("Prepare Oracle rsgb") {
                            echo "init rsgb schema"
                            sh ".jenkins/db-prepare-rsgb.sh"
                        }

                        stage("Prepare Oracle topnl") {
                            echo "init topnl schema"
                            sh ".jenkins/db-prepare-topnl.sh"
                        }

                        stage("brmo-persistence tests"){
                            sh "mvn -e -B -Poracle -pl :brmo-persistence -Dtest.persistence.unit=brmo.persistence.oracle test"
                        }

                        stage("datamodel tests"){
                            sh "mvn -e -B -Poracle -pl 'datamodel' resources:testResources compiler:testCompile surefire:test -Dtest='!*UpgradeTest,!P8*'"
                        }

                        stage("bgt-loader Integration Test") {
                            echo "run integratie tests voor bgt-loader module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'bgt-loader'"
                        }

                        stage("brmo-loader Integration Test") {
                            echo "run integratie tests voor brmo-loader module"
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-loader' -Dit.test=!TopNLIntegrationTest"
                        }

                        lock('tomcat-tcp9091') {
                            stage("brmo-service Integration Test") {
                                echo "run integratie tests voor brmo-service module"
                                timeout(40) {
                                    try {
                                        sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-service'"
                                    } catch (Exception e) {
                                        currentBuild.result = 'UNSTABLE'
                                    }
                                }
                            }

                            stage("brmo-soap Integration Test") {
                                echo "run integratie tests voor brmo-soap module"
                                sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-soap'"
                            }

                            stage("brmo-stufbg204 Integration Test") {
                                echo "run integratie tests voor brmo-stufbg204 module"
                                sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-stufbg204'"
                            }
                        }

                        stage("brmo-commandline Integration Test") {
                            sh "mvn -Djava.security.egd=file:/dev/./urandom -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-commandline'"
                        }

                        stage("Cleanup Database") {
                            sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                            sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top10nl/top10nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top50nl/top50nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top100nl/top100nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                            sh "sqlplus -l -S top250nl/top250nl@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                        }


                        stage("Upgrade Database Test") {
                            sh ".build/ci/getlastRelease.sh"
                            sh ".jenkins/setup-old.sh"
                            sh "\".jenkins/execute-upgrades-oracle.sh\" staging"
                            sh "\".jenkins/execute-upgrades-oracle.sh\" rsgb"
                            sh "\".jenkins/execute-upgrade-extras-oracle.sh\" rsgb"
                            sh "mvn -e -B -Poracle -pl 'datamodel' resources:testResources compiler:testCompile surefire:test -Dtest='*UpgradeTest'"
                        }

                        stage("Cleanup Database 2") {
                            sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                            sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.26:15210/XE < ./.jenkins/clear-schema.sql"
                        }

                        configFileProvider([
                            configFile(
                                fileId: 'local.P8.properties',
                                targetLocation: 'datamodel/src/test/resources/local.P8.properties'
                                )
                            ]) {
                            lock('rsgb-p8') {
                                stage("P8 datamodel Integration Test") {
                                    sh "mvn -Pp8 process-test-resources -pl 'datamodel'"
                                    sh "./datamodel/target/pgsql-reinstall-rsgb-views.sh"
                                    sh "mvn -e verify -B -Pp8 -T1 -pl 'datamodel' -fae"
                                }
                            }
                        }
                        /* sh "docker stop oracle-brmo" */
                    }
                }
            }

            if (env.BRANCH_NAME == 'master' && env.NODE_NAME == 'master') {
                stage("Docker image build & push") {
                    echo "Maak een docker image van master branch als we op de Jenkins master node draaien"
                    sh "mvn install -Dmaven.test.skip=true -B -V -e -fae -q"
                    sh "mvn deploy -B -pl :docker -P docker"
                }
            }

            stage('Publish Test Results') {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml'
            }

            stage('Publish Test Coverage') {
                jacoco exclusionPattern: '**/*Test.class', execPattern: '**/target/**.exec'
                sh "curl -s https://codecov.io/bash | bash"
            }

            stage('OWASP Dependency Check') {
                echo "Uitvoeren OWASP dependency check"
                sh "mvn org.owasp:dependency-check-maven:aggregate"
                dependencyCheckPublisher failedNewCritical: 1, failedNewHigh: 1, failedNewLow: 2, failedNewMedium: 2, failedTotalCritical: 1, failedTotalHigh: 1, failedTotalLow: 5, failedTotalMedium: 5, pattern: '**/dependency-check-report.xml'
            }

            cleanWs cleanWhenFailure: false, cleanWhenNotBuilt: false, cleanWhenUnstable: false, notFailBuild: true
        }
    }
}
