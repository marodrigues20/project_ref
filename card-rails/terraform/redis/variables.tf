variable "service_name" {
  type = "string"
}

variable "team_name" {
  type = "string"
}

variable "redis_name" {
  type = "string"
  default = "card-rails-redis"
}

variable "redis_engine_version" {
  type = "string"
  default = "5.0.4"
}

variable "redis_node_type" {
  type = "string"
  default = "cache.m3.xlarge"
}

variable "redis_num_node_groups" {
  type = "string"
  default = 1
}

variable "redis_parameter_group_name" {
  type = "string"
  default = "default.redis5.0"
}

variable "redis_port" {
  type = "string"
  default = 6379
}

variable "redis_replication_group_id" {
  type = "string"
  default = "card-rails-redis"
  description = "card-rails"
}

variable "redis_availability_zones" {
  type = "list"
}

variable "WebSubnetInside1a" {
  type = "string"
}

variable "WebSubnetInside1b" {
  type = "string"
}

variable "WebSubnetInside1c" {
  type = "string"
}

variable "vpc_id" {
  type = "string"
}

variable "db_protocol" {
  default = "TCP"
}

variable "vpc_cidr_block" {
  type = "string"
}

variable "office_cidr_block" {
  type = "string"
}