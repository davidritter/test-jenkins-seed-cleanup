PROJECTS = [
/*     [
        'name': 'whitesource',
        'jenkinsfile': 'etc/jenkins/Jenkinsfile.whitesource',
        'uuid': '6eb62c50-4217-4fe6-a0d2-48e6597d2fd5',
    ],
    [
        'name': 'build_and_test',
        'jenkinsfile': 'etc/jenkins/Jenkinsfile.build_and_test',
        'uuid': '9071da4b-2101-40e9-b823-9476552197fd',
    ], */
]

VIEWS = [
    ['name': 'main', 'regex': /.*main.*/],
    ['name': 'flat', 'regex': /.*/],
]

for (view in VIEWS) {
    listView(view.name) {
        recurse()
        jobs {
            regex(view.regex)
        }
        columns {
            status()
            weather()
            name()
            lastSuccess()
            lastFailure()
            lastDuration()
            buildButton()
        }
    }
}

for (project in PROJECTS) {
    multibranchPipelineJob(project.name) {
        branchSources {
            branchSource {
                buildStrategies {
                    buildNamedBranches {
                        filters {
                            exact {
                                name (branch == 'origin/main' ? 'main' : '')
                                caseSensitive true
                            }
                        }
                    }
                }
                source {
                    github {
                        id project.uuid
                        credentialsId 'github-p4components-app'
                        repoOwner 'p4components'
                        repository 'test-jenkins-seed-cleanup'
                        repositoryUrl 'https://github.com/davidritter/test-jenkins-seed-cleanup.git'
                        configuredByUrl true
                        traits {
                            gitHubBranchDiscovery {
                                strategyId 1
                            }
                            gitHubPullRequestDiscovery {
                                strategyId 1
                            }
                            headWildcardFilter {
                                includes '*'
                                excludes '_.*'
                            }
                            cloneOptionTrait {
                                extension {
                                    shallow true
                                    noTags true
                                    depth 1
                                    reference ''
                                    timeout 10
                                }
                            }
                            wipeWorkspaceTrait()
                        }
                    }
                }
            }
        }
        factory {
            workflowBranchProjectFactory {
                scriptPath(project.jenkinsfile)
            }
        }
        orphanedItemStrategy {
            discardOldItems {
                daysToKeep(14)
                numToKeep(50)
            }
        }
        if (branch == 'origin/main') {
            triggers {
                periodicFolderTrigger {
                    interval('1h')
                }
            }
        }
    }
}
