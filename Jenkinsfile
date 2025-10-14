pipeline {
    agent { label 'Linux-slave-1' }
    tools {
        jdk 'Java21'
        maven 'Maven-3.8.7'  
    }
    environment {
        SONAR_PROJECT_KEY = 'Bookmyseat-Review-Service-Backend'
        SONAR_CACHE = "${env.WORKSPACE}/.sonar-cache"
        DOCKER_IMAGE = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/bookmyseatreviewservice:${BUILD_NUMBER}"
        AWS_REGION = "${AWS_REGION}"
        AWS_ACCOUNT_ID = "${AWS_ACCOUNT_ID}"
      //  MAVEN_CACHE = "${env.HOME}/.m2/repository"  // Maven dependency cache
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'git@github.com:ALMGHAS/bookmyseat-review-service.git',
                        credentialsId: 'BookmySeat-Review-ssh-cred'
                    ]]
                ])
            }
        }
        /*
        stage('Restore Cache & Install Dependencies') {
            steps {
                script {
                    // Restore Maven cache if stashed from previous run
                    try {
                        unstash('maven-cache')
                        echo "Restored Maven cache from previous run"
                    } catch (Exception e) {
                        echo "No cache found, resolving dependencies"
                        sh 'mvn dependency:resolve'  // Install/resolve dependencies
                    }
                }
            }
        }
        
        */
        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'  // Compile sources
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'  // Run unit tests and generate JaCoCo coverage report
            }
            post {
                always {
                    // Publish JaCoCo coverage report
                    jacoco(
                        execPattern: 'target/site/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        inclusionPattern: '**/*.class'
                    )
                    // Publish JUnit test results
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'  // Create executable JAR artifact
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false  // Archive JAR for download/tracking
                }
            }
        }
        
        stage('SAST Scan') {
            steps {
                script {
                    sh "mkdir -p ${SONAR_CACHE}"
                    withSonarQubeEnv('SonarQube2') {  // Assumes SonarQube server configured in Jenkins
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.projectName=Bookmyseat-Review-Service-Backend \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.junit.reportPaths=target/surefire-reports \
                            -Dsonar.jacoco.reportPaths=target/site/jacoco/jacoco.xml \
                        """
                    }
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true  // Fails build if quality gate fails
                    }
                }
            }
        }
        stage('Container Security Scan') {
            steps {
                script {
                    withCredentials([
                        string(credentialsId: 'AWS_ACCESS_KEY_ID', variable: 'AWS_ACCESS_KEY_ID'),
                        string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                        
                        ]) {
                        sh '''
                        # Create reports folder if not exists
                        mkdir -p reports
                        
                            set -e 
                        # Ensure AWS credentials are available as environment variables
                        export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                        export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
                        export AWS_REGION=$AWS_REGION
                        
                        aws ecr get-login-password --region $AWS_REGION  | \
                        docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
                        
                        echo "Building image..."
                        docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/bookmyseatreviewservice:${BUILD_NUMBER} .
                        
                        echo "Pushing image..."
                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/bookmyseatreviewservice:${BUILD_NUMBER}
                        sleep 2
                        
                        # Download Trivy HTML template
                        curl -L -o reports/html.tpl https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl
                
                     echo "🔍 Running Trivy vulnerability + secret scan (remote mode)..."
                     trivy image --exit-code 1 --no-progress \
                        --severity HIGH,CRITICAL \
                        --cache-dir /tmp/trivy-cache \
                        --timeout 10m \
                        --scanners vuln,secret \
                        --username AWS \
                        --password "$(aws ecr get-login-password --region $AWS_REGION)" \
                        $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/bookmyseatreviewservice:${BUILD_NUMBER} \
                        --format json -o reports/trivy-report.json
                        
                        echo "Generating readable HTML report..."
                            trivy convert \
                            --format template \
                            --template reports/html.tpl \
                            -o reports/trivy-report.html \
                            reports/trivy-report.json 
                        '''
                    }
                }
            }   
            
        post {
                 always {
            // Publish HTML reports (assumes JaCoCo/Surefire generate them)
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'jacoco/index.html',
                reportName: 'Coverage Report'
            ])
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site',
                reportFiles: 'surefire-report.html',
                reportName: 'Test Report'
            ])
            /*
            // Cache Maven modules for next run
            stash includes: "${MAVEN_CACHE}/**", name: 'maven-cache', useDefaultExcludes: false
            echo "Cached Maven dependencies for next run"
            // Cleanup
            sh "docker rmi movie-service:${BUILD_NUMBER} || true"
            sh "rm -f ~/.docker/config.json || true"
            sh 'mvn clean'  // Clean target dir
            */
        }
      }
     }        
    }
}
