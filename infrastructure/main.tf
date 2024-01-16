provider "azurerm" {
  features {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "cft_vnet"
  subscription_id            = var.aks_subscription_id
}

locals {
  vaultName = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "cmc_key_vault" {
  name = local.vaultName
  resource_group_name = local.vaultName
}

data "azurerm_key_vault_secret" "notify_api_key" {
  name = "notify-api-key"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "claim-store-s2s-secret"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "db_password" {
  name = "claim-store-db-password"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "staff_email" {
  name = "staff-email"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "staff_email_legal_rep" {
  name = "staff-email-legal-rep"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "live_support_email" {
  name = "live-support-email"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "milo_recipient" {
  name = "milo-recipient"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa_email_sealed_claim" {
  name = "rpa-email-sealed-claim"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa-email-breathing-space" {
  name = "rpa-email-breathing-space"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa_email_more_time_requested" {
  name = "rpa-email-more-time-requested"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa_email_defence_response" {
  name = "rpa-email-response"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa_email_ccj" {
  name = "rpa-email-ccj"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "rpa_email_paid_in_full" {
  name = "rpa-email-paid-in-full"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "anonymous_caseworker_username" {
  name = "anonymous-caseworker-username"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "anonymous_caseworker_password" {
  name = "anonymous-caseworker-password"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "system_update_username" {
  name = "system-update-username"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "system_update_password" {
  name = "system-update-password"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "oauth_client_secret" {
  name = "citizen-oauth-client-secret"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "launch_darkly_sdk_key" {
  name = "launchDarkly-sdk-key"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault_secret" "sendgrid_api_key" {
  name = "sendgrid-api-key"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-password" {
  name      = "cmc-db-password"
  value     = module.database.postgresql_password
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_key_vault" "send_grid" {
  provider = azurerm.send-grid

  name                = var.env != "prod" ? "sendgridnonprod" : "sendgridprod"
  resource_group_name = var.env != "prod" ? "SendGrid-nonprod" : "SendGrid-prod"
}

data "azurerm_key_vault_secret" "send_grid_api_key" {
  provider = azurerm.send-grid


  key_vault_id = data.azurerm_key_vault.send_grid.id
  name         = var.env != "prod" ? "hmcts-cmc-api-key" : "cmc-api-key"
}

resource "azurerm_key_vault_secret" "sendgrid_api_key-2" {
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
  name         = "sendgrid-api-key-2"
  value        = data.azurerm_key_vault_secret.send_grid_api_key.value
}

module "database" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = var.product
  location = var.location
  env = var.env
  postgresql_user = "cmc"
  database_name = var.database-name
  postgresql_version = var.postgresql_version
  sku_name = var.database_sku_name
  sku_tier = "GeneralPurpose"
  storage_mb = var.database_storage_mb
  common_tags = var.common_tags
  subscription = var.subscription
}

// DB version 11
module "database-v11" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${var.product}-db-v11"
  name  = "cmc-db-v11"
  location = var.location
  env = var.env
  postgresql_user = "cmc"
  database_name = var.database-name
  postgresql_version = "11"
  sku_name = var.database_sku_name
  sku_tier = "GeneralPurpose"
  storage_mb = var.database_storage_mb
  common_tags = var.common_tags
  subscription = var.subscription
}

resource "azurerm_key_vault_secret" "cmc-db-password-v11" {
  name      = "cmc-db-password-v11"
  value     = module.database-v11.postgresql_password
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

data "azurerm_application_insights" "cmc" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-${var.env}"
}

resource "azurerm_key_vault_secret" "appinsights_connection_string" {
  name         = "appinsights-connection-string"
  value        = data.azurerm_application_insights.cmc.connection_string
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}


# FlexiServer v15
module "db-v15" {
  providers = {
    azurerm.postgres_network = azurerm.cft_vnet
  }

  source               = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "CFT"
  name                 = "cmc-db-v15"
  product              = var.product
  env                  = var.env
  component            = var.component
  common_tags          = var.common_tags
  pgsql_version        = 15


  pgsql_databases = [
      {
        name    = var.database-name
      }
    ]
  pgsql_server_configuration = [
      {
        name  = "azure.extensions"
        value = "plpgsql,pg_stat_statements,pg_buffercache"
      }
    ]

    pgsql_sku            = var.pgsql_sku
    pgsql_storage_mb     = var.pgsql_storage_mb

}

resource "azurerm_key_vault_secret" "cmc-db-password-v15" {
  name         = "cmc-db-password-v15"
  value        = module.db-v15.password
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-username-v15" {
  name         = "cmc-db-username-v15"
  value        = module.db-v15.username
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-host-v15" {
  name         = "cmc-db-host-v15"
  value        = module.db-v15.fqdn
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}
