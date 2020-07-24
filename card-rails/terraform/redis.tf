module "redis" {
  source = "redis"
  service_name = "${var.service_name}"
  team_name = "${var.team_name}"
  vpc_cidr_block = "${var.vpc_cidr_block}"
  office_cidr_block = "${var.office_cidr_block}"

  #cluster properties
  redis_availability_zones = ["${data.aws_availability_zones.available.names}"]

  #networking
  WebSubnetInside1a = "${data.aws_cloudformation_stack.network.outputs["WebSubnetInside1a"]}"
  WebSubnetInside1b = "${data.aws_cloudformation_stack.network.outputs["WebSubnetInside1b"]}"
  WebSubnetInside1c = "${data.aws_cloudformation_stack.network.outputs["WebSubnetInside1c"]}"
  vpc_id = "${data.aws_cloudformation_stack.network.outputs["VpcId"]}"
}