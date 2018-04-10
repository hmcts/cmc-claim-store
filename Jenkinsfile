#!groovy

//noinspection GroovyAssignabilityCheck Jenkins API requires this format
properties(
  [
    [$class: 'GithubProjectProperty', projectUrlStr: 'https://github.com/hmcts/cmc-claim-store/'],
    pipelineTriggers([[$class: 'GitHubPushTrigger']]),
    [
      $class: 'BuildDiscarderProperty', strategy: [
        $class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '7', numToKeepStr: '10'
      ]
    ]
  ],
)
@Library('Reform')

def channel = '#cmc-tech-notification'

timestamps {
  milestone()
  lock(resource: "claim-store-${env.BRANCH_NAME}", inversePrecedence: true) {
    node {
      try {

        stage('Checkout') {
          deleteDir()
          checkout scm
        }

        stage('Package (JAR)') {
          versioner.addJavaVersionInfo()
          sh "./gradlew installDist bootRepackage"
        }

        stage('Package (Docker)') {
          claimStoreVersion = dockerImage imageName: 'cmc/claim-store-api'
          claimStoreDatabaseVersion = dockerImage imageName: 'cmc/claim-store-database', context: 'docker/database'
        }

      } catch (err) {
        notifyBuildFailure channel: channel
        throw err
      } finally {
        step([$class           : 'InfluxDbPublisher',
              customProjectName: 'CMC Claimstore',
              target           : 'Jenkins Data'])
      }
    }
    milestone()
  }
  notifyBuildFixed channel: channel
}
