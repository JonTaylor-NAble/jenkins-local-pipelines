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
        terraform -chdir=./cicd/pipelines/terraform/ show -out=terraform.tfplan
        cat ./cicd/pipelines/terraform/terraform.tfplan
        '''
       script{
                import groovy.json.JsonSlurper
       }

      }
    }
  }
  post {
    always {
      cleanWs()
    }
  }
}