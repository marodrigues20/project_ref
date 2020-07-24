module "rds" {
  source = "rds"

  #database properties
  rds_endpoint = "${module.rds.rds_cluster_endpoint}"
  rds_cluster_instance_count = "${var.rds_cluster_instance_count}"
}