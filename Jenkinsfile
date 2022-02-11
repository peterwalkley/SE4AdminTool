node {
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    tools {
      jdk 'JDK_1.8'
    }
    def mvn = tool 'maven';
    withSonarQubeEnv() {

      sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=SEAdminTool"
    }
  }
}