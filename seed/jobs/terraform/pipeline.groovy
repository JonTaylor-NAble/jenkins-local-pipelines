#!/usr/bin/env groovy

pipelineJob{
    agent any
    stages{
        stage('Seed Job'){
            agent any
            steps{
                script{
                    northstar.buildSeedJobs(null);
                }
            }
        }
    }

}
