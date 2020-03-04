timestamps {
    node {
        properties([
            [$class: 'jenkins.model.BuildDiscarderProperty', strategy: 
                [$class: 'LogRotator', artifactDaysToKeepStr: '8', artifactNumToKeepStr: '3', daysToKeepStr: '15', numToKeepStr: '5']
            ]
        ]);

        final def jdks = ['OpenJDK11','JDK8']

        stage('Prepare') {
            sh "ulimit -a"
            sh "free -h"
            checkout scm
        }

        stage('Prepare data') {
            sh ".jenkins/data-prepare-topnl.sh"
        }

        lock('brmo-single-build') {
            jdks.eachWithIndex { jdk, indexOfJdk ->
                final String jdkTestName = jdk.toString()

                withEnv(["JAVA_HOME=${ tool jdkTestName }", "PATH+MAVEN=${tool 'Maven CURRENT'}/bin:${env.JAVA_HOME}/bin"]) {

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

                            stage("datamodel tests"){
                                sh "mvn -e -B -Poracle -pl 'datamodel' resources:testResources compiler:testCompile surefire:test -Dtest='!*UpgradeTest,!P8*'"
                            }

                            stage("bgt-gml-loader Integration Test: ${jdkTestName}") {
                                echo "run integratie tests voor bgt-gml-loader module"
                                sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'bgt-gml-loader'"
                            }

                            stage("brmo-loader Integration Test: ${jdkTestName}") {
                                echo "run integratie tests voor brmo-loader module"
                                sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-loader' -Dit.test=!TopNLIntegrationTest"
                            }

                            lock('brmo-tomcat-9091') {
                                stage("brmo-service Integration Test: ${jdkTestName}") {
                                    echo "run integratie tests voor brmo-service module"
                                    timeout(20) {
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
                                    sh "\".jenkins/execute-upgrade-extras-oracle.sh\" rsgb"
                                    sh "\".jenkins/execute-upgrades-oracle.sh\" rsgbbgt"
                                    sh "mvn -e -B -Poracle -pl 'datamodel' resources:testResources compiler:testCompile surefire:test -Dtest='*UpgradeTest'"
                                }

                                stage("Cleanup Database 2: ${indexOfJdk}") {
                                    sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                                    sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./.jenkins/clear-schema.sql"
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
                                            sh "./datamodel/target/reinstall-rsgb-views-pgsql.sh"
                                            sh "mvn -e verify -B -Pp8 -T1 -pl 'datamodel' -fae"
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (jdkTestName == 'OpenJDK11') {
                        stage("cleanup Java 11 packages") {
                            echo "Verwijder de Java 11 build artifacts uit lokale repo"
                            sh "mvn build-helper:remove-project-artifact"
                        }
                    }
                }
            }
        }

        withEnv(["JAVA_HOME=${ tool 'JDK8' }", "PATH+MAVEN=${tool 'Maven CURRENT'}/bin:${env.JAVA_HOME}/bin"]) {
            stage('Publish Results') {
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
