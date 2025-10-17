folder('Banking-App') {
    description('Folder for Banking Application Pipelines')
}

def environments = ['SIT', 'UAT', 'PROD']

environments.each { env ->
    pipelineJob("Banking-App/Pipeline-${env}") {
        description("Pipeline for ${env} environment of Banking App")

        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url('https://github.com/korupon/br-engineer-lab.git')
                            credentials('github-token')
                        }
                        branch('Banking-App-CI-CD')
                    }
                }
                scriptPath("Seed-Banking-Jobs/Jenkinsfile-Pipeline-${env}")
            }
        }

        triggers {
            scm('H/5 * * * *')  // Optional: poll every 5 mins
        }
    }
}
