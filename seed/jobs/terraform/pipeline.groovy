multibranchPipelineJob('terraform-pipeline') {
  branchSources {
        branchSource {
            source {
                github {
                    id('terraform-pipeline')
                    repoOwner('JonTaylor-NAble')
                    repository('jenkins-local-pipelines')
                    credentialsId('github-account')
                    buildOriginBranch(true)
                    buildOriginPRHead(true)
                    repositoryUrl('')
                    configuredByUrl(false)
                }
            }
            strategy {
            allBranchesSame {
                    props {
                        suppressAutomaticTriggering {
                            strategy('INDEXING')
                        }
                    }
                }
            }
        }
  }

  orphanedItemStrategy {
    discardOldItems {
      daysToKeep(30)
      numToKeep(30)
    }
  }

  factory {
    workflowBranchProjectFactory {
      scriptPath('cicd/pipelines/terraform/terraform.groovy')
    }
  }

  configure {
    def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
    traits << 'org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait' {
      strategyId(1) // Enable support for discovering github branches on this repo
    }
    traits << 'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
      strategyId(2) // Enable support for discovering PullRequests to this github repo
    }
    traits << 'jenkins.plugins.git.traits.CleanBeforeCheckoutTrait' {
      extension(class: 'hudson.plugins.git.extensions.impl.CleanBeforeCheckout') {
        deleteUntrackedNestedRepositories(true)
      }
    }
  }
}