# Postgresql v14 server.
# https://github.com/hmcts/terraform-module-postgresql-flexible

provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "postgres_network"
  subscription_id            = var.aks_subscription_id
}

module "postgresql" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }
  source        = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env           = var.env
  product       = var.product
  component     = var.component
  business_area = "cft"
  pgsql_databases = [
    {
      name : "cmc"
    }
  ]
  pgsql_version        = "14"
  admin_user_object_id = var.jenkins_AAD_objectId
  common_tags          = var.common_tags
}

resource "azurerm_key_vault_secret" "cmc-db-username-v14" {
  name         = "cmc-db-username-v14"
  value        = module.postgresql.username
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-password-v14" {
  name         = "cmc-db-password-v14"
  value        = module.postgresql.password
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-host-v14" {
  name         = "cmc-db-host-v14"
  value        = module.postgresql.fqdn
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-port-v14" {
  name         = "cmc-db-port-v14"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}

resource "azurerm_key_vault_secret" "cmc-db-database-v14" {
  name         = "cmc-db-database-v14"
  value        = "cmc"
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}
