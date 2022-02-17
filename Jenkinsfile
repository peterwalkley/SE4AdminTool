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
            steps {
                sh "mvn sonar:sonar -Dsonar.projectKey=SniperEliteAdmin -Dsonar.host.url=http://centosmaster:9000 -Dsonar.login=17cd0c2f1cf42a4bf1e16fbab236272ab169c4d6"
            }
        }
    }
}
