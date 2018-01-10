variable "product" {
  type = "string"
  default = "cmc"
}

variable "microservice" {
  type = "string"
  default = "claim-store"
}

variable "location" {
  type = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "pdf-service-url" {
  default = "https://not-a-real-host.com"
  description = "Pdf service URL"
}

variable "document-management-url" {
  default = "https://not-a-real-host.com"
  description = "Document management URL"
}

variable "database-name" {
  default = "postgres"
}

variable "ilbIp" {}

variable "component" {
  type = "string"
  default = "backend"
}

variable "tenant_id" {
  type = "string"
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "client_id" {
  type = "string"
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}
