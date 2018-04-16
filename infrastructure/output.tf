output "vaultUri" {
  value = "${local.vaultUri}"
}

output "vaultName" {
  value = "${local.vaultName}"
}

output "idam_api_url" {
  value = "${var.idam_api_url}"
}

output "s2s_url" {
  value = "${var.s2s_url}"
}

output "frontend_base_url" {
  value = "${var.frontend_url}"
}

output "oauth2_s2s_top_secret" {
  value = "${data.vault_generic_secret.s2s_secret.data["value"]}"
}
