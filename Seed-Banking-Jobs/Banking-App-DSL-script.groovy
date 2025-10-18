def envs = ["SIT", "UAT", "PROD"]

envs.each { envName ->
    pipelineJob("Banking-App/Pipeline-${envName}") {
        description("Pipeline for ${envName} environment of Banking App")
        parameters {
            stringParam('ENV', envName, 'Target deployment environment (SIT/UAT/PROD)')
        }
        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url('https://github.com/korupon/br-engineer-lab.git')
                        }
                        branch('*/Banking-App-CI-CD')
                    }
                }
                scriptPath('Jenkinsfile')
            }
        }
        triggers {
            scm('H/5 * * * *') // optional: poll SCM every 5 mins
        }
    }
}
