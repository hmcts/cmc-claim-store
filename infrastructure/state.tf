terraform {
  backend "azurerm" {}

    required_providers {
      azurerm = {
        source  = "hashicorp/azurerm"
        version = "=2.49.0"
      }
      random = {
        source = "hashicorp/random"
      }
    }
}

provider "azurerm" {
  alias = "send-grid"
  features {}
}
