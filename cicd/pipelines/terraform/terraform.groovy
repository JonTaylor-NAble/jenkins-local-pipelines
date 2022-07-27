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
          
          sh 'terraform -chdir=./cicd/pipelines/terraform/ plan -out tf.plan'
          
          tee(file: "tfPlan.log"){
            sh 'terraform -chdir=./cicd/pipelines/terraform/ show -json tf.plan'
          }

          def resultString = readFile(file: 'tfPlan.log');


          def requiresWarning = checkForJenkinsMasterUpdates(resultString);

           if(requiresWarning){
            timeout(time: 5, unit: "MINUTES") {
                input message: 'THIS WILL TRIGGER A RESTART OF THE JENKINS BUILD. CHECK WITH THE BUILD TEAMS BEFORE APPROVING. Do you want the deploy to Proceed?', ok: 'Yes'
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
      sh 'rm -f tfPlan.log'
      withCredentials([gitUsernamePassword(credentialsId: 'github-account', gitToolName: 'git-tool')]) {
            sh 'git config --global user.email "jonathan.taylor@n-able.com"'
            sh 'git config --global user.name "Jon Taylor"'
            sh 'git add .'
            sh 'git commit -m "Post build commit"'
            sh 'git push origin HEAD:main'
      }

      cleanWs()
    }
  }
}

def checkForJenkinsMasterUpdates(planOutputJSON){

        def results = planOutputJSON.split('\n')

        def enhancedWarning = false;
        def triggeringChange;
        def output;

        for (def result in results){
          try{
            output = readJSON text:result;
          } catch (Exception ex){
          }
        }

        if (output){
          if (output.resource_changes){
            for (def resource_change in output.resource_changes){
              if (resource_change.change){
                if (resource_change.change.actions.indexOf('update') > -1){
                  
                  def manifestBeforeStr = resource_change.change.before.manifest;
                  def manifestAfterStr = resource_change.change.after.manifest;

                  def manifestBefore = readJSON text:manifestBeforeStr;
                  def manifestAfter = readJSON text:manifestAfterStr;

                  if (manifestBefore.kind == 'Jenkins'){
                    if (manifestBefore.spec && manifestBefore.spec.master){
                      def compBefore = manifestBefore.spec.master;
                      def compAfter = manifestAfter.spec.master;

                      if (compBefore != compAfter){
                        enhancedWarning = true;
                        triggeringChange = compAfter;
                        echo "Master node config is changed"
                      } 
                    }
                  } 
                }
              }
            }
          }
        } else {
          echo "Invalid TF Plan JSON"
        }

        return enhancedWarning;
}