output "vaultUri" {
  value = "${data.azurerm_key_vault.cmc_key_vault.vault_uri}"
}

output "vaultName" {
  value = "${local.vaultName}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "${local.s2sUrl}"
}

output "frontend_base_url" {
  value = "${var.frontend_url}"
}
