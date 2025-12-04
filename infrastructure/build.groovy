String dockerCred = "DockerCred"

pipeline {
    agent {
        label 'Local'
    }
    options {
        ansiColor('xterm')
    }
    stages {
        stage('Build') {
            steps {
                script {
                    sh "chmod +x startTestDb.sh"
                    sh "chmod +x stopTestDb.sh"
                    try {
                        sh "./startTestDb.sh"
                        sh "mvn clean package -T 1C -ntp -U"
                    } catch (Exception e) {
                        println e
                    } finally {
                        sh './stopTestDb.sh'
                    }
                }
            }
        }
        stage('SQ') {
            steps {
                echo 'Send result to SQ'
            }
        }
        stage('Docker build and upload') {
            steps {
                script{
                    withCredentials([usernamePassword(credentialsId: dockerCred, passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        // Login to Docker Hub
                        sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"

                        // Build your Docker image
                        sh "docker build -t andreyznsk/app:llm.latest -f helm/DockerFile ."

                        // Push the image to Docker Hub
                        sh "docker push andreyznsk/app:llm.latest"

                        // Logout from Docker Hub (optional but good practice)
                        sh "docker logout"
                    }
                }
            }
        }

    }
}