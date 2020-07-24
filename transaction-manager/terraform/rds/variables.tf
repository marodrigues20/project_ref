variable "region" {
  description = "awsregion"
  default     = "eu-west-1"
}

data "aws_caller_identity" "current" {
}

data "aws_cloudformation_stack" "web_network_stack" {
  name = "web-network"
}

variable "service_name" {
  default = "transaction-manager"
}

variable "team_name" {
  default = "FT8"
}

variable "rds_endpoint" {
  type = "string"
}
