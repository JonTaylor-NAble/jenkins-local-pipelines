#!/usr/bin/env groovy

pipelineJob('build-example-tf') {
    displayName('Build Example Terraform')

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
            scriptPath('cicd/pipelines/terraform/terraform.groovy')
        }
    }
}