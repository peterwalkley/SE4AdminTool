pipeline {
    agent any

    tools {
        maven "maven"
        jdk "JDK_1.8"
    }

    stages {
        stage('build') {
            steps {
                sh "java -version"
                sh "mvn -version"
                sh "mvn clean install"
            }
        }
        stage('sonar') {
            tools {
                maven "maven"
                jdk "JDK_1.17"
            }
            withSonarQubeEnv() {
              sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=SniperEliteAdmin"
            }
        }
    }
}
