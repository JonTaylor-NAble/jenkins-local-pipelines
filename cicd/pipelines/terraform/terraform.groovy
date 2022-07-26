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
        sh 'cd ./cicd/pipelines/terraform > terraform init > terraform plan -out=terraform.tfplan'
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