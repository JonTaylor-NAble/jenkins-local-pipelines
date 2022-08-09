#!/usr/bin/env groovy

def northstar = library('jenkins-local-shared-lib').northstar
northstar.buildSeedJobs(null);

