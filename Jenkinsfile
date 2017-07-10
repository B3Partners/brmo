timestamps {
    
    properties([
        [$class: 'jenkins.model.BuildDiscarderProperty', strategy: [$class: 'LogRotator', 
            artifactDaysToKeepStr: '8', 
            artifactNumToKeepStr: '3', 
            daysToKeepStr: '15', 
            numToKeepStr: '5']
        ]]);
    
    node {
        withEnv(["JAVA_HOME=${ tool 'JDK8' }", "PATH+MAVEN=${tool 'Maven 3.5.0'}/bin:${env.JAVA_HOME}/bin"]) {

            stage('Prepare') {
                sh "ulimit -a"
                sh "free -m"
                checkout scm
            }

            stage('Build') {
                echo "Building branch: ${env.BRANCH_NAME}"
                sh "mvn clean install -Dmaven.test.skip=true -B -V -e -fae  -q -Poracle -pl '!brmo-dist'"
            }

            stage('Test') {
                echo "Running unit tests"
                sh "mvn -e test -B -Poracle -pl '!brmo-dist'"
            }

            lock('brmo-oracle') {

                stage('Prepare Oracle Databases') {
                    echo "cleanup schema's"
                    sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
                    sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
                    
                    echo "init schema's"
                    sh ".jenkins/db-prepare-staging.sh"
                    sh ".jenkins/db-prepare-rsgb.sh"
                    sh ".jenkins/db-prepare-rsgbbgt.sh"
                }

                stage('bgt-gml-loader Integration Test') {
                    echo "run integratie tests voor bgt-gml-loader module"
                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'bgt-gml-loader'"
                }

                stage('brmo-loader Integration Test') {
                    echo "run integratie tests voor brmo-loader module"
                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-loader'"
                }

                stage('brmo-service Integration Test') {
                    echo "run integratie tests voor brmo-service module"
                    timeout(10) {
                        try {
                            sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-service'"
                        } catch (Exception e) {
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                }

                /* TODO */
                stage('brmo-soap Integration Test') {
                    echo "run integratie tests voor brmo-soap module"
                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-soap'"
                }

                stage('brmo-commandline Integration Test') {
                    echo "run integratie tests voor brmo-commandline module"
                    sh "mvn -e verify -B -Poracle -T1 -Dtest.onlyITs=true -pl 'brmo-commandline'"
                }

                stage('Cleanup Database') {
                    sh "sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                    sh "sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.41:1521/DB01 < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                    sh "sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
                }
            }

            stage('Publish Results') {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml'
            }
        }
    }
}
