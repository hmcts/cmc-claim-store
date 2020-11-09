variable "product" {}

variable "raw_product" {
  default = "cmc" // jenkins-library overrides product for PRs and adds e.g. pr-118-cmc
}

variable "microservice" {
  default = "claim-store"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "database-name" {
  default = "cmc"
}

variable "database_sku_name" {
  default = "GP_Gen5_2"
}

variable "database_storage_mb" {
  default = "51200"
}

variable "postgresql_version" {
  default = "10"
}

variable "common_tags" {
  type = map(string)
}

variable "mail-host" {
  default = "mta.reform.hmcts.net"
}

variable "ilbIp" {}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "subscription" {}

variable "jenkins_AAD_objectId" {
  type                        = "string"
  description                 = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default = ""
}

