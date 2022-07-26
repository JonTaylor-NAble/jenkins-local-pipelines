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
        sh 'terraform -chdir=./cicd/pipelines/terraform/ init -no-color'
      }
    }
    stage('approval'){
      steps{
        script{
          tee(file: "tfPlan.log"){
            sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -no-color -json'
          }
          def resultString = readFile(file: 'tfPlan.log')
          def results = resultString.split('\n')
          def outputs = [];

          def enhancedWarning = false;
          def triggeringChange;

          for (def result in results){
            try{
              def thisOutput = readJSON text:result;
              outputs.add(thisOutput)
            } catch (Exception ex){
            }
          }

          for (def output in outputs){
            if (output.change){
              if (output.change.resource.addr == 'docker_container.nginx'){
                enhancedWarning = true;
                triggeringChange = output;
              }
            }

          }


          if (enhancedWarning){

            echo "Enhanced warning - change caused by:"
            echo triggeringChange.toString();
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
