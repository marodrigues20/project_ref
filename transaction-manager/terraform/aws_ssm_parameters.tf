variable "ssm_base_name" {
  default = "transaction-manager"
}

resource "aws_ssm_parameter" "spring_datasource_url" {
  name = "${var.ssm_base_name}.MYSQL_DATASOURCE_URL"
  description = "Spring datasource url for ${var.ssm_base_name}"
  type = "String"
  value = "jdbc:mysql://${module.rds.rds_cluster_endpoint}/transactionmanager"
}

resource "aws_ssm_parameter" "spring_datasource_username" {
  name = "${var.ssm_base_name}.MYSQL_DATASOURCE_USERNAME"
  description = "Spring datasource username for ${var.ssm_base_name}"
  type = "SecureString"
  value = "${module.rds.db_user}"
}

resource "aws_ssm_parameter" "spring_datasource_password" {
  name = "${var.ssm_base_name}.MYSQL_DATASOURCE_PASSWORD"
  description = "Spring datasource password for ${var.ssm_base_name}"
  type = "SecureString"
  value = "${module.rds.master_password}"
}