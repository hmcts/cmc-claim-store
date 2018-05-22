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

  previewVaultName = "${var.product}-claim-store"
  nonPreviewVaultName = "${var.product}-claim-store-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  nonPreviewVaultUri = "${module.claim-store-vault.key_vault_uri}"
  previewVaultUri = "https://cmc-claim-store-aat.vault.azure.net/"
  vaultUri = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultUri : local.nonPreviewVaultUri}"
}

data "vault_generic_secret" "notify_api_key" {
  path = "secret/${var.vault_section}/cmc/notify_api_key"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/cmcClaimStore"
}

data "vault_generic_secret" "db_password" {
  path = "secret/${var.vault_section}/cmc/claim-store/database/password"
}

data "vault_generic_secret" "staff_email" {
  path = "secret/${var.vault_section}/cmc/claim-store/staff_email"
}

data "vault_generic_secret" "rpa_email" {
  path = "secret/${var.vault_section}/cmc/claim-store/rpa_email"
}

data "vault_generic_secret" "anonymous_citizen_username" {
  path = "secret/${var.vault_section}/ccidam/idam-api/cmc/anonymouscitizen/user"
}

data "vault_generic_secret" "anonymous_citizen_password" {
  path = "secret/${var.vault_section}/ccidam/idam-api/cmc/anonymouscitizen/password"
}

data "vault_generic_secret" "system_update_username" {
  path = "secret/${var.vault_section}/ccidam/idam-api/cmc/systemupdate/user"
}

data "vault_generic_secret" "system_update_password" {
  path = "secret/${var.vault_section}/ccidam/idam-api/cmc/systemupdate/password"
}

data "vault_generic_secret" "oauth_client_secret" {
  path = "secret/${var.vault_section}/ccidam/idam-api/oauth2/client-secrets/cmc-citizen"
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

  app_settings = {
    //    logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"

    // db vars
    CLAIM_STORE_DB_HOST = "${var.db_host}"
    CLAIM_STORE_DB_PORT = "5432"
    CLAIM_STORE_DB_USERNAME = "claimstore"
    CLAIM_STORE_DB_PASSWORD = "${data.vault_generic_secret.db_password.data["value"]}"
    CLAIM_STORE_DB_NAME = "${var.database-name}"
    CLAIM_STORE_DB_CONNECTION_OPTIONS = "?ssl"

    // idam
    IDAM_API_URL = "${var.idam_api_url}"
    IDAM_S2S_AUTH_URL = "${local.s2sUrl}"
    IDAM_S2S_AUTH_TOTP_SECRET = "${data.vault_generic_secret.s2s_secret.data["value"]}"

    IDAM_ANONYMOUS_CASEWORKER_USERNAME = "${data.vault_generic_secret.anonymous_citizen_username.data["value"]}"
    IDAM_ANONYMOUS_CASEWORKER_PASSWORD = "${data.vault_generic_secret.anonymous_citizen_password.data["value"]}"
    IDAM_SYSTEM_UPDATE_USER_USERNAME = "${data.vault_generic_secret.system_update_username.data["value"]}"
    IDAM_SYSTEM_UPDATE_USER_PASSWORD = "${data.vault_generic_secret.system_update_password.data["value"]}"
    OAUTH2_CLIENT_SECRET = "${data.vault_generic_secret.oauth_client_secret.data["value"]}"

    // notify
    GOV_NOTIFY_API_KEY = "${data.vault_generic_secret.notify_api_key.data["value"]}"

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
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST= "${var.mail-host}"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "${data.vault_generic_secret.staff_email.data["value"]}"

    // robot notifications
    RPA_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    RPA_NOTIFICATIONS_RECIPIENT = "${data.vault_generic_secret.rpa_email.data["value"]}"
    // feature toggles
    CLAIM_STORE_TEST_SUPPORT_ENABLED = "${var.env == "prod" ? "false" : "true"}"
    FEATURE_TOGGLES_EMAILTOSTAFF = "true"
  }
}

module "claim-store-vault" {
  source = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name = "${local.vaultName}"
  product = "${var.product}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.claim-store-api.resource_group_name}"
  product_group_object_id = "68839600-92da-4862-bb24-1259814d1384"
}
