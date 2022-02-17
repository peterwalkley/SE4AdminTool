pipeline {
    agent any

    tools {
        maven "maven"
        jdk "JDK_1.8"
    }
    environment {
        SONAR_KEY = credentials('SNIPER_ELITE_ADMIN_SONAR_ID')
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
                sh "mvn sonar:sonar -Dsonar.projectKey=SniperEliteAdmin -Dsonar.host.url=http://centosmaster:9000 -Dsonar.login=${SONAR_KEY}"
            }
        }
    }
}
