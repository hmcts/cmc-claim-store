terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 5.3.0"
    }
    random = {
      source = "hashicorp/random"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.9.0"
    }
  }
}

provider "azurerm" {
  alias           = "send-grid"
  subscription_id = var.send_grid_subscription
  features {}
}
