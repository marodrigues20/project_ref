variable "aws_region" {
  description = "aws region"
  default = "eu-west-1"
}

variable "team_name" {
  type = "string"
  default = "Cards"
}

variable "service_name" {
  type = "string"
  default = "card-rails"
}

variable "vpc_cidr_block" {
  type = "string"
  default = "10.0.0.0/8"
}

variable "office_cidr_block" {
  type = "string"
  default = "192.168.0.0/16"
}

data "aws_cloudformation_stack" "network" {
  name = "web-network"
}

data "aws_availability_zones" "available" {
}
