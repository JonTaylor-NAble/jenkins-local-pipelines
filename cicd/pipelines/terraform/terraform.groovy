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
        sh 'terraform -chdir=./cicd/pipelines/terraform/ init -no-color'
      }
    }
    stage('approval'){
      steps{

        script{
          
          sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -no-color -out tf.plan'

          def requiresWarning = checkForJenkinsMasterUpdates('tf.plan', './cicd/pipelines/terraform/');

           if(requiresWarning){
            timeout(time: 5, unit: "MINUTES") {
                input message: '\033[31mTHIS WILL TRIGGER A RESTART. CHECK WITH THE BUILD TEAMS BEFORE APPROVING.\033[0m Do you want the deploy to Proceed?', ok: 'Yes'
            }
           } else {
            timeout(time: 5, unit: "MINUTES") {
                input message: 'Do you want the deploy to Proceed?', ok: 'Yes'
            }
           }

        }
      }
    }
    stage('apply'){
      steps{
        sh 'terraform -chdir=./cicd/pipelines/terraform/ apply -auto-approve -no-color'
      }
    }
  }
  post {
    always {
      withCredentials([gitUsernamePassword(credentialsId: 'github-account', gitToolName: 'git-tool')]) {
            sh 'git add .'
            sh 'git commit -m "Post build commit"'
            sh 'git push origin HEAD:main'
      }

      cleanWs()
    }
  }
}