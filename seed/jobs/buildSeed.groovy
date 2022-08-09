def gitCredentialsId      = 'github-account'
def jobsRepoName          = 'https://github.com/jontaylor-nable/jenkins-local-pipelines.git'
def sharedLibraryRepoName = 'https://github.com/jontaylor-nable/jenkins-local-shared-lib.git'


checkout([
    $class: 'GitSCM', 
    branches: [[name: '*/main']], 
    doGenerateSubmoduleConfigurations: false,
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'shared-library']], 
    submoduleCfg: [],
    userRemoteConfigs: [[credentialsId:  gitCredentialsId, url: sharedLibraryRepoName ]]
    ])

git url: jobsRepoName, changelog: false, credentialsId: gitCredentialsId, poll: false, branch: 'main'
