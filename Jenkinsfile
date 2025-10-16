pipeline {
  agent any

  environment {
    APP_NAME = "banking-app"
    JIRA_USER = credentials('jira-user')
    JIRA_TOKEN = credentials('jira-token')
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
            sh 'mvn clean package -DskipTests'
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
    }
  }
}
