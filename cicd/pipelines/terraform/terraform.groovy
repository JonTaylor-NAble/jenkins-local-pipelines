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
        sh 'kubectl '
        sh 'terraform -chdir=./cicd/pipelines/terraform/ init'
        tee(file: "tfPlan.log"){
            sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -no-color -json'
        }
      }
    }
    stage('approval'){
      steps{
        script{
          def resultString = new File('tfPlan.log').text
          def results = resultString.split('\n')
          def outputs = [];

          def enhancedWarning = false;
          def triggeringChange;

          for (def result in results){
            outputs.add(readJSON text:result)
          }

          for (def output in outputs){
            if (output.change){
              if (output.change.resource.addr == 'ddocker_container.nginx'){
                enhancedWarning = true;
                triggeringChange = output;
              }
            }

          }

          echo "Enhanced warning - change caused by:"
          echo triggeringChange.toString();

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
