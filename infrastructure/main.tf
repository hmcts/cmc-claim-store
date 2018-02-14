provider "vault" {
//  # It is strongly recommended to configure this provider through the
//  # environment variables described above, so that each user can have
//  # separate credentials set in the environment.
//  #
//  # This will default to using $VAULT_ADDR
//  # But can be set explicitly
  address = "https://vault.reform.hmcts.net:6200"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  vault_section = "${var.env == "prod" ? "prod" : "test"}"

  idam_api_url = "${var.env == "prod" ? var.prod-idam-api-url : var.test-idam-api-url}"
  s2s_url = "${var.env == "prod" ? var.prod-s2s-url : var.test-s2s-url}"
  frontend_url = "${var.env == "prod" ? var.prod-frontend-url : var.nonprod-frontend-url}"
}

data "vault_generic_secret" "notify_api_key" {
  path = "secret/${local.vault_section}/cmc/notify_api_key"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/${local.vault_section}/ccidam/service-auth-provider/api/microservice-keys/cmcClaimStore"
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
    IDAM_API_URL = "${local.idam_api_url}"
    IDAM_S2S_AUTH_URL = "${local.s2s_url}"
    IDAM_S2S_AUTH_TOTP_SECRET = "${data.vault_generic_secret.s2s_secret.data["value"]}"

    // notify
    GOV_NOTIFY_API_KEY = "${data.vault_generic_secret.notify_api_key.data["value"]}"

    // urls
    FRONTEND_BASE_URL = "${local.frontend_url}"
    PDF_SERVICE_URL = "http://cmc-pdf-service-${var.env}.service.${local.aseName}.internal"
    DOCUMENT_MANAGEMENT_API_GATEWAY_URL = "false"
    // CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-${var.env}.service.${local.aseName}.internal"
    CORE_CASE_DATA_API_URL = "false"

    // mail
    SPRING_MAIL_HOST = "${var.mail-host}"
    SPRING_MAIL_PORT = "25"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
    SPRING_MAIL_TEST_CONNECTION = "false"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "civilmoneyclaims+cnp@gmail.com"

    // feature toggles
    CLAIM_STORE_TEST_SUPPORT_ENABLED = "${var.env == "prod" ? "false" : "true"}"
  }
}

module "claim-store-database" {
  source = "git@github.com:contino/moj-module-postgres?ref=master"
  product = "${var.product}-ase"
  location = "West Europe"
  env = "${var.env}"
  postgresql_user = "claimstore"
  postgresql_database = "${var.database-name}"
}

module "claim-store-vault" {
  source              = "git@github.com:contino/moj-module-key-vault?ref=master"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.claim-store-api.resource_group_name}"
}
