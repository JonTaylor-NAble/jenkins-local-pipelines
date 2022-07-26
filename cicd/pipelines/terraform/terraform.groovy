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
            sh 'terraform -chdir=./cicd/pipelines/terraform/ plan'
        }
      }
    }
    stage('approval'){
      steps{
        script{
          tfResults = parseTerraformLog(readFile("tfPlan.log"));

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

import java.util.regex.Pattern
import java.util.regex.Matcher

def parseTerraformLog(String contents) {
  HashMap results = [
    type: "none",
    add: 0,
    change: 0,
    delete: 0,
    line: "",
    is_change: false,
  ]

  Pattern regex = Pattern.compile(/.*(Plan|Apply).*:\s(\d+).*,\s(\d+).*,\s(\d+).*/)
  Matcher matcher = regex.matcher(contents)
  if (matcher.find()) {
    results["type"] = matcher.group(1)
    results["add"] = matcher.group(2).toInteger()
    results["change"] = matcher.group(3).toInteger()
    results["destroy"] = matcher.group(4).toInteger()
    results["line"] = matcher.group(0).substring(matcher.group(0).indexOf(":")+1).trim()
    results["is_change"] = (results["add"] + results["change"] + results["destroy"]).asBoolean()
  } else {
    regex = Pattern.compile(/.*(Destroy).*:\s(\d+)\s.*/)
    matcher = regex.matcher(contents)
    if (matcher.find()) {
      results["type"] = matcher.group(1)
      results["destroy"] = matcher.group(2).toInteger()
      results["line"] = matcher.group(0).substring(matcher.group(0).indexOf("!")+1).trim()
      results["is_change"] = (results["add"] + results["change"] + results["destroy"]).asBoolean()
    }
  }
  return results
}