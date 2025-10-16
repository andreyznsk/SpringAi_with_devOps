pipeline {
    agent {
        label 'Local'
    }
    stages {
        stage('Build') {
            steps {
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
        stage('SQ') {
            steps {
                echo 'Send result to SQ'
            }
        }
        stage('Docker build') {
            steps {
                echo 'Deploying....'
            }
        }

        stage('Docker upload') {
            steps {
                echo 'Deploying....'
            }
        }

    }
}