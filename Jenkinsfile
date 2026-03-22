pipeline {
    agent any

    tools {
            maven 'maven3'

    }



    environment {
        // Jenkins Credentials ID
        DOCKER_AUTH = credentials('docker-hub-secret')
        // Full image name (DockerHubUser/RepoName)
        IMAGE_NAME = "zakariaemoufid/learn-link"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Fetching code from GitHub..."
                checkout scm
            }
        }

        stage('Build & Tests') {
            steps {
                echo "Compiling and running all tests..."
                // 'verify' handles unit tests, integration tests, and creates the JAR
                sh 'mvn clean verify -Pintegration-test'
            }
            post {
                always {
                    // Collects both Surefire (Unit) and Failsafe (Integration) reports
                    junit 'target/*-reports/*.xml'

                    // Generates the JaCoCo visual report in Jenkins
                    jacoco execPattern: 'target/*.exec',
                           classPattern: 'target/classes',
                           sourcePattern: 'src/main/java'
                }
            }
        }

        stage('Docker Build Image') {
            steps {
                script {
                    echo "Building the Docker image..."
                    // We build the image locally on the Jenkins agent first
                    // We tag it with 'latest' AND the unique 'BUILD_ID'
                    sh "docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${env.BUILD_ID} ."
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    echo "Logging into Docker Hub..."
                    sh "echo \$DOCKER_AUTH_PSW | docker login -u \$DOCKER_AUTH_USR --password-stdin"

                    echo "Pushing images..."
                    sh "docker push ${IMAGE_NAME}:latest"
                    sh "docker push ${IMAGE_NAME}:${env.BUILD_ID}"
                }
            }
        }
    }

    post {
        always {
            echo "Cleaning up..."
            sh "docker logout"
            cleanWs() // Deletes the workspace to save disk space
        }
    }
}