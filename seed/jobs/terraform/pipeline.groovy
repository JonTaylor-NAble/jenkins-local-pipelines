#!/usr/bin/env groovy

pipeline{
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
