data "aws_security_group" "default" {
  vpc_id = "${data.aws_cloudformation_stack.network.outputs["VpcId"]}"
  filter {
    name = "group-name"
    values = ["default"]
  }
}

data "aws_cloudformation_stack" "network" {
  name = "web-network"
}

data "aws_availability_zones" "available" {}
variable "rds_cluster_instance_count" {
  default = "2"
}