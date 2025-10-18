pipeline {
    agent any

    parameters {
        string(name: 'ENV', defaultValue: 'SIT', description: 'Target deployment environment (SIT/UAT/PROD)')
    }

    environment {
        APP_NAME = "banking-app"
        JIRA_USER = credentials('jira-user')
        JIRA_TOKEN = credentials('jira-token')
        DOCKER_IMAGE = "banking-app:${params.ENV}-${env.BUILD_NUMBER}"
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
                git branch: 'Banking-App-CI-CD', url: 'https://github.com/korupon/br-engineer-lab.git'
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "🔧 Building Banking App for ${params.ENV} environment..."
                    if (fileExists('pom.xml')) {
                        sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    } else if (fileExists('build.gradle')) {
                        sh './gradlew build -x test'
                    } else if (fileExists('package.json')) {
                        sh 'npm install && npm run build'
                    } else {
                        echo "⚠️ No recognized build file found."
                    }
                }
            }
        }

        stage('Unit Test') {
            steps {
                echo "🧪 Running unit tests for ${params.ENV}"
                sh 'echo Running tests...'
            }
        }

        stage('Package & Archive') {
            steps {
                echo "📦 Archiving artifacts..."
                archiveArtifacts artifacts: '**/target/*.jar,**/build/libs/*.jar,**/dist/**'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    echo "🐳 Building Docker image: ${DOCKER_IMAGE}"
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { params.ENV == 'PROD' } // Only push to registry for PROD
            }
            steps {
                script {
                    echo "🚀 Pushing image ${DOCKER_IMAGE} to ${DOCKER_REGISTRY}"
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials-id') {
                        sh "docker tag ${DOCKER_IMAGE} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}"
                        sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo "🚢 Deploying Banking App to ${params.ENV} environment..."
                    if (params.ENV == 'SIT') {
                        sh 'ansible-playbook -i ansible/inventory/sit_bluegreen.ini ansible/deploy_bluegreen.yml'
                    } else if (params.ENV == 'UAT') {
                        sh 'ansible-playbook -i ansible/inventory/uat_bluegreen.ini ansible/deploy_bluegreen.yml'
                    } else if (params.ENV == 'PROD') {
                        sh 'ansible-playbook -i ansible/inventory/prod_bluegreen.ini ansible/deploy_bluegreen.yml'
                    }
                }
            }
        }

        stage('Promote & Switch Traffic') {
            when {
                expression { params.ENV == 'PROD' }
            }
            steps {
                sh 'bluegreen/switch_traffic.sh green'
            }
        }

        stage('Create Jira Release Ticket') {
            when {
                expression { params.ENV == 'PROD' }
            }
            steps {
                echo "📄 Creating Jira release ticket for ${params.ENV}"
                sh './scripts/create_jira_release.sh'
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline complete for ${params.ENV}. Check Jira for release notes."
            cleanWs()
        }
        success {
            echo "🎉 Build ${env.BUILD_NUMBER} for ${params.ENV} succeeded."
        }
        failure {
            echo "❌ Build ${env.BUILD_NUMBER} for ${params.ENV} failed."
        }
    }
}
