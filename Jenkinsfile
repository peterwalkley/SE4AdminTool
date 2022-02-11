node {
    tools {
      jdk 'JDK_1.8'
    }
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    def mvn = tool 'maven';
    withSonarQubeEnv() {

      sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=SEAdminTool"
    }
  }
}