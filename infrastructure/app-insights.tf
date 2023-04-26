data "azurerm_application_insights" "cmc" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-${var.env}"
}

resource "azurerm_key_vault_secret" "appinsights_connection_string" {
  name         = "appinsights-connection-string"
  value        = data.azurerm_application_insights.cmc.connection_string
  key_vault_id = data.azurerm_key_vault.cmc_key_vault.id
}
