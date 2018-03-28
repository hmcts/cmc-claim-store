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
    node('moj_centos_large2') {
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
            sh "./gradlew assemble"
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
          claimStoreRPMVersion = packager.javaRPM('claim-store', 'build/libs/claim-store.jar',
            'springboot', 'src/main/resources/application.yml')
          version = "{claim_store_buildnumber: ${claimStoreRPMVersion} }"

          onMaster {
            packager.publishJavaRPM('claim-store')
          }
        }

        onPR {
          stage('Integration Tests') {
            integrationTests.execute([
              'CLAIM_STORE_API_VERSION'     : claimStoreVersion,
              'CLAIM_STORE_DATABASE_VERSION': claimStoreDatabaseVersion,
              'TESTS_TAG'                   : '@quick'
            ])
          }
        }

        onMaster {
          milestone()
          lock(resource: "CMC-deploy-demo", inversePrecedence: true) {
            stage('Deploy (Demo)') {
              ansible.runDeployPlaybook(version, 'demo')
            }
            stage('Smoke test (Demo)') {
              smokeTests.executeAgainst(env.CMC_DEMO_APPLICATION_URL)
            }
          }
          milestone()
        }
      } catch (err) {
        archiveArtifacts 'build/reports/**/*.html'
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
