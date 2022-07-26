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
        sh 'terraform -chdir=./cicd/pipelines/terraform/ init'
        tee(file: "tfPlan.log"){
            sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -no-color -json'
        }
      }
    }
    stage('approval'){
      steps{
        script{
          def tfResults = readJSON file:'tfPlan.log'

          if (tfResults["add"]){
            echo "Approval Needed"
            timeout(time: 5, unit: "MINUTES") {
              input message: 'Do you want the deploy to Proceed?', ok: 'Yes'
            }
          } else {
            echo "No Approval Needed"
          }
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
