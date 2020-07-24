resource "aws_ssm_parameter" "ssm_redis_port" {
  name = "${var.service_name}.REDIS_PORT"
  description = "Redis port for ${var.redis_name}"
  type = "String"
  value = "${var.redis_port}"
}

resource "aws_ssm_parameter" "ssm_redis_host" {
  name = "${var.service_name}.REDIS_HOST"
  description = "Redis host for ${var.redis_name}"
  type = "String"
  value = "${aws_elasticache_replication_group.card_rails_redis_replication_group.primary_endpoint_address}"
}

resource "aws_ssm_parameter" "ssm_redis_auth_token" {
  name = "${var.service_name}.REDIS_AUTH_TOKEN"
  description = "Redis authorisation token for ${var.redis_name}"
  type = "String"
  value = "${aws_elasticache_replication_group.card_rails_redis_replication_group.auth_token}"
}