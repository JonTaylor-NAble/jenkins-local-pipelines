pipeline {
  agent any
  tools {
    terraform 'Terraform'
  }
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
        sh '''
        terraform -chdir=./cicd/pipelines/terraform/ init
        terraform -chdir=./cicd/pipelines/terraform/ plan -out=terraform.tfplan
        cat ./cicd/pipelines/terraform/terraform.tfplan
        '''
      }
    }
  }
  post {
    always {
      cleanWs()
    }
  }
}