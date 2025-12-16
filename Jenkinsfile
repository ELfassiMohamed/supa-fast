pipeline {
    agent {
        docker {
            image 'my-maven-git:latest'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    stages {
        stage('Checkout') {
            steps {
                echo 'Code already checked out by Jenkins SCM'
            }
        }
        stage('Build') {
            steps {
                script {
                    def currentDir = pwd()
                    echo "Current directory: ${currentDir}"
                    sh 'mvn clean test package'
                    sh "java -jar target/maven-pipeline-0.0.1-SNAPSHOT.jar"
                }
            }
        }
    }
}