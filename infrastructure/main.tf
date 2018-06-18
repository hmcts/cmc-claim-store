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

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  s2sUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  sendLetterUrl = "http://rpe-send-letter-service-${local.local_env}.service.${local.local_ase}.internal"
  pdfserviceUrl = "http://cmc-pdf-service-${local.local_env}.service.${local.local_ase}.internal"

  ccdCnpUrl = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
  ccdApiUrl = "${var.env == "demo" ? local.ccdCnpUrl : "false"}"

  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
}

data "azurerm_key_vault" "cmc_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "notify_api_key" {
  name = "notify-api-key"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "claim-store-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "db_password" {
  name = "claim-store-db-password"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "staff_email" {
  name = "staff-email"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email" {
  name = "rpa-email"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "anonymous_caseworker_username" {
  name = "anonymous-caseworker-username"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "anonymous_caseworker_password" {
  name = "anonymous-caseworker-password"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_update_username" {
  name = "system-update-username"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "system_update_password" {
  name = "system-update-password"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "oauth_client_secret" {
  name = "citizen-oauth-client-secret"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

module "scheduler-database" {
  source = "git@github.com:hmcts/moj-module-postgres?ref=uksouth-v10"
  product = "${var.product}-scheduler"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "scheduler"
  database_name = "scheduler"
  version = "10"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
}

module "claim-store-api" {
  source = "git@github.com:hmcts/moj-module-webapp.git?ref=RPE-389/local-cache"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  is_frontend = false
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  subscription = "${var.subscription}"
  capacity = "${var.capacity}"

  app_settings = {
    //    logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"

    // db vars
    CLAIM_STORE_DB_HOST = "${var.db_host}"
    CLAIM_STORE_DB_PORT = "5432"
    CLAIM_STORE_DB_USERNAME = "claimstore"
    CLAIM_STORE_DB_PASSWORD = "${data.azurerm_key_vault_secret.db_password.value}"
    CLAIM_STORE_DB_NAME = "${var.database-name}"
    CLAIM_STORE_DB_CONNECTION_OPTIONS = "?ssl"

    SCHEDULER_DB_HOST = "${module.scheduler-database.host_name}"
    SCHEDULER_DB_PORT = "${module.scheduler-database.postgresql_listen_port}"
    SCHEDULER_DB_NAME = "${module.scheduler-database.postgresql_database}"
    SCHEDULER_DB_USERNAME = "${module.scheduler-database.user_name}"
    SCHEDULER_DB_PASSWORD = "${module.scheduler-database.postgresql_password}"

    // idam
    IDAM_API_URL = "${var.idam_api_url}"
    IDAM_S2S_AUTH_URL = "${local.s2sUrl}"
    IDAM_S2S_AUTH_TOTP_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"

    IDAM_CASEWORKER_ANONYMOUS_USERNAME = "${data.azurerm_key_vault_secret.anonymous_caseworker_username.value}"
    IDAM_CASEWORKER_ANONYMOUS_PASSWORD = "${data.azurerm_key_vault_secret.anonymous_caseworker_password.value}"
    IDAM_CASEWORKER_SYSTEM_USERNAME = "${data.azurerm_key_vault_secret.system_update_username.value}"
    IDAM_CASEWORKER_SYSTEM_PASSWORD = "${data.azurerm_key_vault_secret.system_update_password.value}"
    OAUTH2_CLIENT_SECRET = "${data.azurerm_key_vault_secret.oauth_client_secret.value}"

    // notify
    GOV_NOTIFY_API_KEY = "${data.azurerm_key_vault_secret.notify_api_key.value}"

    // urls
    FRONTEND_BASE_URL = "${var.frontend_url}"
    RESPOND_TO_CLAIM_URL = "${var.respond_to_claim_url}"
    PDF_SERVICE_URL = "${local.pdfserviceUrl}"
    DOCUMENT_MANAGEMENT_API_GATEWAY_URL = "false"
    CORE_CASE_DATA_API_URL = "${local.ccdApiUrl}"
    SEND_LETTER_URL = "${var.env == "saat" || var.env == "sprod" ? "false" : local.sendLetterUrl}"

    // mail
    SPRING_MAIL_HOST = "${var.mail-host}"
    SPRING_MAIL_PORT = "25"
    SPRING_MAIL_TEST_CONNECTION = "false"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST = "${var.mail-host}"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "${data.azurerm_key_vault_secret.staff_email.value}"

    // robot notifications
    RPA_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    RPA_NOTIFICATIONS_RECIPIENT = "${data.azurerm_key_vault_secret.rpa_email.value}"
    // feature toggles
    CLAIM_STORE_TEST_SUPPORT_ENABLED = "${var.env == "prod" ? "false" : "true"}"
    FEATURE_TOGGLES_EMAILTOSTAFF = "${var.enable_staff_email}"

    ROOT_APPENDER = "CMC"
  }
}
