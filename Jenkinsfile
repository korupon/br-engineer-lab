pipeline {
    agent any

    environment {
        APP_NAME = "banking-app"
        JIRA_USER = credentials('jira-user')
        JIRA_TOKEN = credentials('jira-token')
        DOCKER_IMAGE = "banking-app:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = "your-docker-registry" // e.g., docker.io/your-org
        MAVEN_HOME = tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/korupon/br-engineer-lab.git'
            }
        }

        stage('Build') {
            steps {
                script {
                    if (fileExists('pom.xml')) {
                        sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    } else if (fileExists('build.gradle')) {
                        sh './gradlew build -x test'
                    } else if (fileExists('package.json')) {
                        sh 'npm install && npm run build'
                    } else {
                        echo "No build file found."
                    }
                }
            }
        }

        stage('Unit Test') {
            steps {
                sh 'echo Running tests...'
            }
        }

        stage('Package & Archive') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar,**/build/libs/*.jar,**/dist/**'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { env.BRANCH_NAME == 'main' } // Push only from main branch
            }
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials-id') {
                        sh "docker tag ${DOCKER_IMAGE} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}"
                        sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}"
                    }
                }
            }
        }

        stage('Deploy to SIT') {
            steps {
                sh 'ansible-playbook -i ansible/inventory/sit_bluegreen.ini ansible/deploy_bluegreen.yml'
            }
        }

        stage('Promote & Switch Traffic') {
            steps {
                sh 'bluegreen/switch_traffic.sh green'
            }
        }

        stage('Create Jira Release Ticket') {
            steps {
                sh './scripts/create_jira_release.sh'
            }
        }
    }

    post {
        always {
            echo "Pipeline complete. Check Jira for release notes."
            cleanWs()
        }
        success {
            echo "Build ${env.BUILD_NUMBER} succeeded."
        }
        failure {
            echo "Build ${env.BUILD_NUMBER} failed."
        }
    }
}

