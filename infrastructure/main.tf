resource "random_string" "database_password" {
  length = 32
  special = true
}

//resource "azurerm_key_vault_secret" "database_password" {
//  name = "database-password"
//  value = "${random_string.database_password.result}"
//  vault_uri = "${module.key-vault.key_vault_uri}"
//}

module "claim-store-api" {
  source = "git@github.com:contino/moj-module-webapp.git"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"

  app_settings = {
    //    logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"

    // db vars
//    CLAIM_STORE_DB_HOST = "${module.claim-store-database.host_name}"
//    CLAIM_STORE_DB_HOST = "${module.claim-store-database.host_name}"
//    CLAIM_STORE_DB_PORT = "${module.claim-store-database.postgresql_listen_port}"
//    POSTGRES_DATABASE = "${module.claim-store-database.postgresql_database}"
//    CLAIM_STORE_DB_USERNAME = "${module.claim-store-database.user_name}"
//    CLAIM_STORE_DB_PASSWORD = "${azurerm_key_vault_secret.database_password.value}"
    CLAIM_STORE_DB_NAME = "${var.database-name}"
    CLAIM_STORE_DB_CONNECTION_OPTIONS = "?ssl"

    // idam
    IDAM_API_URL = "https://unknown-url.reform.hmcts.net"

    // notify
    GOV_NOTIFY_API_KEY = "tbd"

    // urls
    FRONTEND_BASE_URL = "https://unknown-url.reform.hmts.net"
    PDF_SERVICE_URL = "${var.pdf-service-url}"
    DOCUMENT_MANAGEMENT_API_GATEWAY_URL = "${var.document-management-url}"

    // mail
    SPRING_MAIL_HOST = "mail.reform.hmcts.net"
    SPRING_MAIL_PORT = "25"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
    SPRING_MAIL_TEST_CONNECTION = "false"

    // staff notifications
    STAFF_NOTIFICATIONS_SENDER = "noreply@reform.hmcts.net"
    STAFF_NOTIFICATIONS_RECIPIENT = "noreply@reform.hmcts.net"
  }
}

//module "claim-store-database" {
//  source = "git@github.com:contino/moj-module-postgres?ref=master"
//  product = "${var.product}-ase"
//  location = "West Europe"
//  env = "${var.env}"
//  postgresql_user = "claimstore"
//  postgresql_password = "${azurerm_key_vault_secret.database_password.value}"
//  postgresql_database = "${var.database-name}"
//}

//module "key-vault" {
//  source = "git@github.com:contino/moj-module-key-vault?ref=master"
//  product = "${var.product}-${var.microservice}"
//  env = "${var.env}"
//  tenant_id = "${var.tenant_id}"
//  object_id = "${var.client_id}"
//}
