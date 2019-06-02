pipeline {
  agent { label 'master' }
  options {
    timeout(time: 15, unit: 'MINUTES')
    skipDefaultCheckout()
  }

  stages {
    stage ('Build Jenkins plugin') {
      steps {
        checkout([$class: 'GitSCM', branches: scm.branches, extensions: scm.extensions + [[$class: 'CleanCheckout']], userRemoteConfigs: scm.userRemoteConfigs])
        script {
          withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
            if (("master" == env.BRANCH_NAME) || ("develop" == env.BRANCH_NAME) || env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
              // Maven Central deployment:  'mvn -P release clean package verify deploy'
              sh "git rev-parse HEAD > current-commit"
              def currCommit = readFile('current-commit').replace("\n", "").replace("\r", "")
              sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true -Dgit.commit=${currCommit}"
            } else {
              sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package -Dmaven.test.failure.ignore=true"
            }
          }
        }
        archiveArtifacts artifacts: 'target/*.hpi'
      }
    }
    stage ('SonarQube analysis') {
      steps {
        script {
          withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
            withCredentials([string(credentialsId: 'AdminTokenSonarQube', variable: 'SQ_TOKEN')]) {
              if (("master" == env.BRANCH_NAME) || ("develop" == env.BRANCH_NAME)) {
                sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr -Dsonar.login=${env.SQ_TOKEN} -Dsonar.branch.name=${env.BRANCH_NAME} sonar:sonar"
              } else if (env.BRANCH_NAME.startsWith("release") || env.BRANCH_NAME.startsWith("hotfix")) {
                sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr -Dsonar.login=${env.SQ_TOKEN} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=master sonar:sonar"
              } else {
                sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr -Dsonar.login=${env.SQ_TOKEN} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop sonar:sonar"
              }
            }
          }
        }
      }
    }
  }
}
