#!groovy

stage ('Build') {
  node ('master') {
    checkout([
      $class: 'GitSCM',
      branches: scm.branches,
      extensions: scm.extensions + [[$class: 'CleanCheckout']],
      userRemoteConfigs: scm.userRemoteConfigs
    ])

    checkout scm

    withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
      if ("master" == env.BRANCH_NAME) {
        sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
      } else {
        sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package"
      }
    }
    archiveArtifacts artifacts: 'target/*.hpi'
    step([$class: 'Publisher', reportFilenamePattern: '**/testng-results.xml'])

    if ("master" == env.BRANCH_NAME) {
      withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
        sh "mvn -Dsonar.host.url=http://sonar.riverside-software.fr sonar:sonar"
      }
    }
  }
}

