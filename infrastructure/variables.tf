variable "product" {
  default = "cmc"
}

variable "microservice" {
  default = "claim-store"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "document-management-url" {
  default = "false"
}

variable "idam-api-url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

variable "s2s-url" {
  default = "http://betaDevBccidamS2SLB.reform.hmcts.net"
}

variable "ccd-url" {
  default = "http://ccd-data-store-api-${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"
}

variable "frontend-url" {
  default = "https://www.nonprod.moneyclaim.platform.hmcts.net"
}

variable "database-name" {
  default = "postgres"
}

variable "mail-host" {
  default = "mta.reform.hmcts.net"
}

variable "ilbIp" {}

variable "component" {
  default = "backend"
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}
