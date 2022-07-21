#!/usr/bin/env groovy

pipelineJob('build-operator-service-for-jenkins') {
    displayName('Build Operator Service for Jenkins')

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/jontaylor-nable/jenkins-local-pipelines.git')
                    }
                    branches('*/main')
                }
            }
            scriptPath('cicd/pipelines/*.groovy')
        }
    }
}