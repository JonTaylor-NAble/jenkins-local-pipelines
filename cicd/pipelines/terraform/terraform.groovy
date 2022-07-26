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
          tee(file: "tfPlan.log"){
            sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -no-color -json'
          }

          def resultString = readFile(file: 'tfPlan.log');
          def warnIfChanged = ['docker_container.nginx'];

          def requiresWarning = checkPlanForWarning(resultString, warnIfChanged);
          echo requiresWarning

           if(requiresWarning){
            timeout(time: 5, unit: "MINUTES") {
                input message: 'Do you want the deploy to Proceed?', ok: 'Yes'
            }
           } else {
            timeout(time: 5, unit: "MINUTES") {
                input message: 'THIS WILL TRIGGER A RESTART OF THE JENKINS BUILD. CHECK WITH THE BUILD TEAMS BEFORE APPROVING. Do you want the deploy to Proceed?', ok: 'Yes'
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
      cleanWs()
    }
  }
}

def checkPlanForWarning(planOutputJSON, protectedProperties){

        def results = planOutputJSON.split('\n')
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
            if (protectedProperties.indexOf(output.change.resource.addr) > -1){
              echo output.change.resource.addr
              enhancedWarning = true;
              triggeringChange = output;
            }
          }
        }

        return enhancedWarning;
}