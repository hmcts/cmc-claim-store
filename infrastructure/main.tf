locals {
  aseName = "core-compute-${var.env}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  s2sUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  sendLetterUrl = "http://rpe-send-letter-service-${local.local_env}.service.${local.local_ase}.internal"
  pdfserviceUrl = "http://cmc-pdf-service-${local.local_env}.service.${local.local_ase}.internal"

  ccdCnpUrl = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"

  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  sku_size = "${var.env == "prod" || var.env == "sprod" || var.env == "aat" ? "I2" : "I1"}"
  asp_name = "${var.env == "prod" ? "cmc-claim-store-prod" : "${var.product}-${var.env}"}"
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

data "azurerm_key_vault_secret" "live_support_email" {
  name = "live-support-email"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "milo_recipient" {
  name = "milo-recipient"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email_sealed_claim" {
  name = "rpa-email-sealed-claim"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email_more_time_requested" {
  name = "rpa-email-more-time-requested"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email_defence_response" {
  name = "rpa-email-response"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email_ccj" {
  name = "rpa-email-ccj"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "rpa_email_paid_in_full" {
  name = "rpa-email-paid-in-full"
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

resource "azurerm_key_vault_secret" "cmc-db-password" {
  name      = "cmc-db-password"
  value     = "${module.database.postgresql_password}"
  vault_uri = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

module "database" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "cmc"
  database_name = "cmc"
  postgresql_version = "10"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
  common_tags = "${var.common_tags}"
  subscription = "${var.subscription}"
}

module "claim-store-api" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  is_frontend = false
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  subscription = "${var.subscription}"
  capacity = "${var.capacity}"
  common_tags = "${var.common_tags}"
  asp_name = "${local.asp_name}"
  asp_rg = "${local.asp_name}"
  instance_size = "${local.sku_size}"
  enable_ase = "${var.enable_ase}"

  app_settings = {
    //    logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"

    CMC_DB_HOST = "${module.database.host_name}"
    CMC_DB_PORT = "${module.database.postgresql_listen_port}"
    CMC_DB_NAME = "${module.database.postgresql_database}"
    CMC_DB_USERNAME = "${module.database.user_name}"
    CMC_DB_PASSWORD = "${module.database.postgresql_password}"
    CMC_DB_CONNECTION_OPTIONS = "?ssl=true&sslmode=require"

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
    DOCUMENT_MANAGEMENT_URL = "${var.dm_url}"
    DOC_ASSEMBLY_URL = "${var.doc_assembly_api_url}"
    CORE_CASE_DATA_API_URL = "${local.ccdCnpUrl}"
    SEND_LETTER_URL = "${var.env == "saat" || var.env == "sprod" ? "false" : local.sendLetterUrl}"
    FEES_URL = "${var.fees_url}"
    PAY_URL = "${var.payments_url}"

    // mail
    SPRING_MAIL_HOST = "${var.mail-host}"
    SPRING_MAIL_PORT = "25"
    SPRING_MAIL_TEST_CONNECTION = "false"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST = "${var.mail-host}"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "${data.azurerm_key_vault_secret.staff_email.value}"

    // MILO
    MILO_CSV_SENDER = "noreply@reform.hmcts.net"
    MILO_CSV_RECIPIENT = "${data.azurerm_key_vault_secret.milo_recipient.value}"
    MILO_CSV_SCHEDULE = "${var.milo_csv_schedule}"

    // State Transition schedules
    SCHEDULE_STATE_TRANSITION_STAY_CLAIM = "${var.schedule_state-transition_stay-claim}"
    SCHEDULE_STATE_TRANSITION_WAITING_TRANSFER = "${var.schedule_state-transition_waiting-transfer}"

    // robot notifications
    RPA_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    RPA_NOTIFICATIONS_SEALEDCLAIMRECIPIENT = "${data.azurerm_key_vault_secret.rpa_email_sealed_claim.value}"
    RPA_NOTIFICATIONS_MORETIMEREQUESTEDRECIPIENT = "${ data.azurerm_key_vault_secret.rpa_email_more_time_requested.value}"
    RPA_NOTIFICATIONS_RESPONSERECIPIENT = "${data.azurerm_key_vault_secret.rpa_email_defence_response.value}"
    RPA_NOTIFICATIONS_COUNTYCOURTJUDGEMENTRECIPIENT = "${data.azurerm_key_vault_secret.rpa_email_ccj.value}"
    RPA_NOTIFICATIONS_PAIDINFULLRECIPIENT = "${data.azurerm_key_vault_secret.rpa_email_paid_in_full.value}"

    // feature toggles
    CLAIM_STORE_TEST_SUPPORT_ENABLED = "${var.env == "prod" ? "false" : "true"}"
    FEATURE_TOGGLES_SAVE_CLAIM_STATE_ENABLED = "${var.save_claim_state_enabled}"

    //thread pool configs
    ASYNC_MAX_THREADPOOL_SIZE = 50

    ROOT_APPENDER = "CMC"

    DOCUMENT_MANAGEMENT_USERROLES = "caseworker-cmc,citizen"
  }
}
