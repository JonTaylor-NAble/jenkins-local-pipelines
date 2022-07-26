pipeline {
  agent any
  options {
    skipDefaultCheckout(true)
  }
  stages{
    stage('clean workspace') {
      steps {
        cleanWs()
      }
    }
    stage('checkout') {
      steps {
        checkout scm
      }
    }
    stage('terraform') {
      steps {
        sh 'bash ./cicd/pipelines/terraform/terraformw'
      }
    }
  }
  post {
    always {
      cleanWs()
    }
  }
}