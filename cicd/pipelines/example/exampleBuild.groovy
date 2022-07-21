// Jenkinsfile (Declarative Pipeline)
pipeline {
  agent any
  stages {
    stage('Stage 1') {
      steps {
        script {
                timeout(time: 5, unit: "MINUTES") {
              input message: 'Do you want the deploy to Proceed?', ok: 'Yes'
          }
        }
        echo 'Hello world!'
      }
    }
  }
}