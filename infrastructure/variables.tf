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

variable "idam_api_url" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

variable "frontend_url" {
  default = "https://cmc-citizen-frontend-saat.service.core-compute-saat.internal"
}

variable "respond_to_claim_url" {
  default = "https://cmc-citizen-frontend-saat.service.core-compute-saat.internal/first-contact/start"
}

variable "database-name" {
  default = "claimstore"
}

variable "mail-host" {
  default = "mta.reform.hmcts.net"
}

variable "dm_url" {
  default = "false"
}

variable "doc_assembly_api_url" {
  default = "false"
}

variable "fees_url" {
  default = "false"
}

variable "payments_url" {
  default = "false"
}

variable "ilbIp" {}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
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

variable "db_host" {
  default = "test-data-lb.moneyclaim.reform.hmcts.net"
}

variable "capacity" {
  default = "1"
}

variable "enable_staff_email" {
  default = "true"
}

variable "save_claim_state_enabled" {
  default = "false"
}

variable "async_event_operations_enabled" {
  default = "false"
}

variable "directions_questionnaire_enabled" {
  default = "false"
}

variable milo_csv_schedule  {
  default = "-"
}

variable "common_tags" {
  type = "map"
}

variable claim_stayed_schedule  {
  default = "-"
}
