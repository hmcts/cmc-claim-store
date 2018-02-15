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
@Library(['CMC', 'Reform'])
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.RPMTagger
import uk.gov.hmcts.Versioner
import uk.gov.hmcts.cmc.integrationtests.IntegrationTests
import uk.gov.hmcts.cmc.smoketests.SmokeTests

Ansible ansible = new Ansible(this, 'cmc')
Packager packager = new Packager(this, 'cmc')
Versioner versioner = new Versioner(this)

SmokeTests smokeTests = new SmokeTests(this)
IntegrationTests integrationTests = new IntegrationTests(env, this)
def channel = '#cmc-tech-notification'

timestamps {
  milestone()
  lock(resource: "claim-store-${env.BRANCH_NAME}", inversePrecedence: true) {
    node('moj_centos_regular') {
      try {
        def version
        def claimStoreVersion
        def claimStoreDatabaseVersion
        String claimStoreRPMVersion

        stage('Checkout') {
          deleteDir()
          checkout scm
        }

        onMaster {
          stage('Build') {
            sh "./gradlew clean build -x test -x apiTest"
          }

          stage('OWASP dependency check') {
            try {
              sh "./gradlew -DdependencyCheck.failBuild=true dependencyCheck"
            } catch (ignored) {
              archiveArtifacts 'build/reports/dependency-check-report.html'
              notifyBuildResult channel: channel, color: 'warning',
                message: 'OWASP dependency check failed see the report for the errors'
            }
          }

          stage('Test (Unit)') {
            try {
              sh "./gradlew test"
            } finally {
              junit 'build/test-results/test/**/*.xml'
            }
          }
        }

        stage('Test (Api)') {
          try {
            sh """
              export GOV_NOTIFY_API_KEY="dummy-value-for-testing"
              export FRONTEND_BASE_URL="http://localhost:3000"
              ./gradlew apiTest
            """
          } finally {
            junit 'build/test-results/apiTest/**/*.xml'
          }
        }

        stage('Sonar') {
          onPR {
            withCredentials([string(credentialsId: 'jenkins-public-github-api-token-text', variable: 'GITHUB_ACCESS_TOKEN')]) {
              sh """
               ./gradlew -Dsonar.analysis.mode=preview \
                -Dsonar.github.pullRequest=$CHANGE_ID \
                -Dsonar.github.repository=hmcts/cmc-claim-store \
                -Dsonar.github.oauth=$GITHUB_ACCESS_TOKEN \
                -Dsonar.host.url=$SONARQUBE_URL \
                sonarqube
            """
            }
          }

          onMaster {
            sh "./gradlew -Dsonar.host.url=$SONARQUBE_URL sonarqube"
          }
        }

        stage('Package (JAR)') {
          versioner.addJavaVersionInfo()
          sh "./gradlew installDist bootRepackage"
        }

        stage('Package (Docker)') {
          claimStoreVersion = dockerImage imageName: 'cmc/claim-store-api'
          claimStoreDatabaseVersion = dockerImage imageName: 'cmc/claim-store-database', context: 'docker/database'
        }

        stage('Package (RPM)') {
          claimStoreRPMVersion = packager.javaRPM('claim-store', 'build/libs/claim-store-$(./gradlew -q printVersion)-all.jar',
            'springboot', 'src/main/resources/application.yml')
          version = "{claim_store_buildnumber: ${claimStoreRPMVersion} }"

          onMaster {
            packager.publishJavaRPM('claim-store')
          }
        }

        stage('Integration Tests') {
          integrationTests.execute([
            'CLAIM_STORE_API_VERSION'     : claimStoreVersion,
            'CLAIM_STORE_DATABASE_VERSION': claimStoreDatabaseVersion,
            'TESTS_TAG'                   : '@quick'
          ])
        }

        //noinspection GroovyVariableNotAssigned it is guaranteed to be assigned
        RPMTagger rpmTagger = new RPMTagger(this,
          'claim-store',
          packager.rpmName('claim-store', claimStoreRPMVersion),
          'cmc-local'
        )
        onMaster {
          milestone()
          lock(resource: "CMC-deploy-dev", inversePrecedence: true) {
            stage('Deploy (Dev)') {
              ansibleCommitId = ansible.runDeployPlaybook(version, 'dev')
              rpmTagger.tagDeploymentSuccessfulOn('dev')
              rpmTagger.tagAnsibleCommit(ansibleCommitId)
            }
            stage('Smoke test (Dev)') {
              smokeTests.executeAgainst(env.CMC_DEV_APPLICATION_URL)
              rpmTagger.tagTestingPassedOn('dev')
            }
          }

          milestone()
//          lock(resource: "CMC-deploy-demo", inversePrecedence: true) {
//            stage('Deploy (Demo)') {
//              ansible.runDeployPlaybook(version, 'demo')
//            }
//            stage('Smoke test (Demo)') {
//              smokeTests.executeAgainst(env.CMC_DEMO_APPLICATION_URL)
//            }
//          }
//          milestone()
        }
      } catch (err) {
        archiveArtifacts 'build/reports/**/*.html'
        notifyBuildFailure channel: channel
        throw err
      } finally {
        step([$class: 'InfluxDbPublisher',
               customProjectName: 'CMC Claimstore',
               target: 'Jenkins Data'])
      }
    }
    milestone()
  }
  notifyBuildFixed channel: channel
}
