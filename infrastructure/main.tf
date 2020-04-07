provider "azurerm" {
  version = "1.44.0"
}

locals {
  vaultName = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "cmc_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "notify_api_key" {
  name = "notify-api-key"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "claim-store-s2s-secret"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "db_password" {
  name = "claim-store-db-password"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "staff_email" {
  name = "staff-email"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "live_support_email" {
  name = "live-support-email"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "milo_recipient" {
  name = "milo-recipient"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "rpa_email_sealed_claim" {
  name = "rpa-email-sealed-claim"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "rpa_email_more_time_requested" {
  name = "rpa-email-more-time-requested"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "rpa_email_defence_response" {
  name = "rpa-email-response"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "rpa_email_ccj" {
  name = "rpa-email-ccj"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "rpa_email_paid_in_full" {
  name = "rpa-email-paid-in-full"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "anonymous_caseworker_username" {
  name = "anonymous-caseworker-username"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "anonymous_caseworker_password" {
  name = "anonymous-caseworker-password"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "system_update_username" {
  name = "system-update-username"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "system_update_password" {
  name = "system-update-password"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

data "azurerm_key_vault_secret" "oauth_client_secret" {
  name = "citizen-oauth-client-secret"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

resource "azurerm_key_vault_secret" "cmc-db-password" {
  name      = "cmc-db-password"
  value     = "${module.database.postgresql_password}"
  key_vault_id = "${data.azurerm_key_vault.cmc_key_vault.id}"
}

module "database" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}"
  location = "${var.location}"
  env = "${var.env}"
  postgresql_user = "cmc"
  database_name = "${var.database-name}"
  postgresql_version = "${var.postgresql_version}"
  sku_name = "${var.database_sku_name}"
  sku_tier = "GeneralPurpose"
  storage_mb = "${var.database_storage_mb}"
  common_tags = "${var.common_tags}"
  subscription = "${var.subscription}"
}


