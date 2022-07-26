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
        sh 'terraform init -chdir=./cicd/pipelines/terraform/'
        sh 'terraform plan -chdir=./cicd/pipelines/terraform/ -out=terraform.tfplan'
        sh 'cat ./cicd/pipelines/terraform/terraform.tfplan'
      }
    }
  }
  post {
    always {
      cleanWs()
    }
  }
}