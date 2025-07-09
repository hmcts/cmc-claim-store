terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0.0"
    }
    random = {
      source = "hashicorp/random"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.53.1"
    }
  }
}

provider "azurerm" {
  alias           = "send-grid"
  subscription_id = var.send_grid_subscription
  features {}
}
