provider "vault" {
//  # It is strongly recommended to configure this provider through the
//  # environment variables described above, so that each user can have
//  # separate credentials set in the environment.
//  #
//  # This will default to using $VAULT_ADDR
//  # But can be set explicitly
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "notify_api_key" {
  path = "secret/dev/cmc/notify_api_key"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/test/ccidam/service-auth-provider/api/microservice-keys/cmcClaimStore"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}

module "claim-store-api" {
  source = "git@github.com:contino/moj-module-webapp.git"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"

  app_settings = {
    //    logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"

    // db vars
    CLAIM_STORE_DB_HOST = "${module.claim-store-database.host_name}"
    CLAIM_STORE_DB_PORT = "${module.claim-store-database.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.claim-store-database.postgresql_database}"
    CLAIM_STORE_DB_USERNAME = "${module.claim-store-database.user_name}"
    CLAIM_STORE_DB_PASSWORD = "${module.claim-store-database.postgresql_password}"
    CLAIM_STORE_DB_NAME = "${var.database-name}"
    CLAIM_STORE_DB_CONNECTION_OPTIONS = "?ssl"

    // idam
    IDAM_API_URL = "${var.idam-api-url}"
    IDAM_S2S_AUTH_URL = "${var.s2s-url}"
    IDAM_S2S_AUTH_TOTP_SECRET = "${data.vault_generic_secret.s2s_secret.data["value"]}"

    // notify
    GOV_NOTIFY_API_KEY = "${data.vault_generic_secret.notify_api_key.data["value"]}"

    // urls
    FRONTEND_BASE_URL = "${var.frontend-url}"
    PDF_SERVICE_URL = "http://cmc-pdf-service-${var.env}.service.${local.aseName}.internal"
    DOCUMENT_MANAGEMENT_API_GATEWAY_URL = "${var.document-management-url}"
    CORE_CASE_DATA_API_URL = "${var.ccd-url}"

    // mail
    SPRING_MAIL_HOST = "${var.mail-host}"
    SPRING_MAIL_PORT = "25"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
    SPRING_MAIL_TEST_CONNECTION = "false"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "civilmoneyclaims+cnp@gmail.com"
  }
}

module "claim-store-database" {
  source = "git@github.com:contino/moj-module-postgres?ref=random-password"
  product = "${var.product}-ase"
  location = "West Europe"
  env = "${var.env}"
  postgresql_user = "claimstore"
  postgresql_database = "${var.database-name}"
}
