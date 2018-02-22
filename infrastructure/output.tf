output "vaultUri" {
  value = "${module.claim-store-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${module.claim-store-vault.key_vault_name}"
}
