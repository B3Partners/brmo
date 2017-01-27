timestamps {
    node {
        withEnv(["JAVA_HOME=${ tool 'JDK8' }", "PATH+MAVEN=${tool 'Maven 3.3.9'}/bin:${env.JAVA_HOME}/bin"]) {

            stage('Prepare') {
                // checkout([$class: 'GitSCM', branches: [[name: '**']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/B3Partners/brmo.git']]])
                git branch: 'master', url: 'https://github.com/B3Partners/brmo.git'
            }

            stage('Build') {
                sh "mvn install -Dmaven.test.skip=true -B -V -e -fae  -q -Poracle -pl '!brmo-dist'"
            }

            stage('Test') {
                echo "Running unit tests"
                sh "mvn -e test -B -Poracle -pl '!brmo-dist'"
            }

            stage('Prepare Databases') {
                // cleanup
                sh "sqlplus jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                sh "sqlplus jenkins_staging/jenkins_staging@192.168.1.41:1521/DB01 < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                sh "sqlplus jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
                
                // init schema's
                sh ".jenkins/db-prepare-staging.sh"
                sh ".jenkins/db-prepare-rsgb.sh"
                sh ".jenkins/db-prepare-rsgbbgt.sh"
            }

            stage('IntegrationTest') {
                echo "Running integration tests"
                try {
                    // run integratie tests voor bgt-gml-loader module
                    sh "mvn -e verify -B -Poracle -Dtest.onlyITs=true -pl 'bgt-gml-loader'"
                    // run integratie tests voor brmo-loader module
                    sh "mvn -e verify -B -Poracle -Dtest.onlyITs=true -pl 'brmo-loader'"
                    // run integratie tests voor brmo-service module
                    sh "mvn -e verify -B -Poracle -Dtest.onlyITs=true -pl 'brmo-service'"
                } catch (e) {
                    echo "Error running integration tests"
                }
            }

            stage('Deploy') {
                echo "Working on branch: ${env.BRANCH_NAME}"

                switch (env.BRANCH_NAME) {
                  case 'master':
                    env.DEPLOYMENT_ENVIRONMENT = 'prod';
                    break;
                  case 'develop':
                    env.DEPLOYMENT_ENVIRONMENT = 'test';
                    break;
                  default:
                    env.DEPLOYMENT_ENVIRONMENT = 'no_deploy';
                }

                echo "deployment environment: ${env.DEPLOYMENT_ENVIRONMENT}"

                //if (env.DEPLOYMENT_ENVIRONMENT != 'no_deploy') {
                //  catchError { sh './deploy.sh' }
                //}
            }

            stage('Cleanup Database') {
                sh "sqlplus jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 < ./bgt-gml-loader/target/generated-resources/ddl/oracle/drop_rsgb_bgt.sql"
                sh "sqlplus jenkins_staging/jenkins_staging@192.168.1.41:1521/DB01 < ./brmo-persistence/db/drop-brmo-persistence-oracle.sql"
                sh "sqlplus jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./.jenkins/clear-schema.sql"
            }

        }
    }
}
