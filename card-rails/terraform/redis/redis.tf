resource "random_string" "card_rails_auth_token" {
  length = 32
  special = true
  override_special = "!#$%&"
}

resource "aws_elasticache_subnet_group" "card_rails_redis_subnet_group" {
  name = "${var.redis_name}-subnet-group"
  description = "${var.service_name} redis subnet group"
  subnet_ids = [
    "${var.WebSubnetInside1a}",
    "${var.WebSubnetInside1b}",
    "${var.WebSubnetInside1c}"]
}

resource "aws_elasticache_replication_group" "card_rails_redis_replication_group" {
  automatic_failover_enabled = true
  availability_zones = [
    "${var.redis_availability_zones}"]
  engine = "redis"
  engine_version = "${var.redis_engine_version}"
  node_type = "${var.redis_node_type}"
  parameter_group_name = "${var.redis_parameter_group_name}"
  port = "${var.redis_port}"
  replication_group_id = "${var.redis_replication_group_id}"
  replication_group_description = "${var.service_name} redis cluster"
  security_group_ids = [
    "${aws_security_group.card_rails_security_group.id}"]
  subnet_group_name = "${aws_elasticache_subnet_group.card_rails_redis_subnet_group.name}"
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  maintenance_window = "sun:04:00-sun:05:00"
  number_cache_clusters = 3
  auth_token = "${random_string.card_rails_auth_token.result}"
  tags {
    Name = "${var.redis_name}"
    Owner = "${var.team_name}"
  }
}
