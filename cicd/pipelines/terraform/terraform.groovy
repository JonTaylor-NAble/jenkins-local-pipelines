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
        dir('./cicd/pipelines/terraform/'){
          sh 'terraform init'
        } 
      }
    }
    stage('approval'){
      steps{

        script{

          def requiresWarning = false;

          dir('./cicd/pipelines/terraform/'){
            sh 'terraform plan -no-color -out tf.plan'
            requiresWarning = checkForJenkinsMasterUpdates 'tf.plan';
          }
          
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
        sh 'terraform -chdir=./cicd/pipelines/terraform/ apply -auto-approve'
      }
    }
  }
  post {
    always {
      withCredentials([gitUsernamePassword(credentialsId: 'github-account', gitToolName: 'git-tool')]) {
            sh '''
            set +x
            git add .
            git commit -m "Post-build Commit"
            git push origin HEAD:main
            set -x
            '''
      }

      cleanWs()
    }
  }
}