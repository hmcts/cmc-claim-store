variable "product" {
  type    = "string"
  default = "cmc"
}

variable "microservice" {
  type = "string"
  default = "claim-store"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "database-password" {
  default = "Notarealpassword!"
  description = "Password for microservice database"
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

variable "ssenv" {
  type = "string"
  default = "prd"
}

variable "ilbIp"{}
