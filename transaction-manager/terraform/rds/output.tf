output "rds_cluster_endpoint" {
  value = "${aws_rds_cluster.transaction-manager-aurora-cluster.endpoint}"
}

output "db_user" {
  value = "${var.db_user}"
}

output "master_password" {
  value = "${random_string.master_password.result}"
}