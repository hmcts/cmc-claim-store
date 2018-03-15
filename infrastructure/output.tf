output "vaultUri" {
  value = "${module.claim-store-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${module.claim-store-vault.key_vault_name}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "${var.s2s_url}"
}
