variable "db_user" {
  description = "db user"
  default     = "txnmanager"
}

variable "backtrack_window" {
  default = "259200"
}

variable "backup_retention_period" {
  default = 15
}

variable "preferred_backup_window" {
  default = "23:00-03:00"
}

variable "preferred_maintenance_window" {
  default = "wed:05:00-wed:05:30"
}

variable "rds_cluster_instance_count" {
  default = "2"
}

variable "db_instance" {
  default = "db.r4.large"
}

resource "random_string" "master_password" {
  length = 12
  special = true
  #some special characters are not allowed when creating the DB master password
  override_special = "!#$%&"
}

resource "aws_security_group" "transaction-manager-db-sg" {
  description = "${var.service_name} DB Access"
  name        = "${var.service_name}-db-sg"
  vpc_id      = "${data.aws_cloudformation_stack.web_network_stack.outputs["VpcId"]}"
  ingress {
    from_port = 3306
    to_port   = 3306
    protocol  = "TCP"
    cidr_blocks = ["10.0.0.0/8"]
  }
  egress {
    from_port   =  0
    to_port     =  0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  lifecycle {
    ignore_changes = ["name"]
  }
}

resource "aws_db_subnet_group" "transaction-manager-db-subnet-group" {
  name       = "${var.service_name}-subnet-group"
  subnet_ids = ["${data.aws_cloudformation_stack.web_network_stack.outputs["WebSubnetInside1a"]}",
                "${data.aws_cloudformation_stack.web_network_stack.outputs["WebSubnetInside1b"]}"]
  tags {
    Name = "${var.service_name} db subnet group"
  }
}

resource "aws_rds_cluster_parameter_group" "transaction-manager-db-cluster-param-group" {
  name   = "${var.service_name}-db-cluster-param-group"
  family = "aurora5.6"
  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }
  parameter {
    name  = "character_set_client"
    value = "utf8mb4"
  }
  parameter {
    name  = "character_set_results"
    value = "utf8mb4"
  }
  parameter {
    name  = "character_set_connection"
    value = "utf8mb4"
  }
  parameter {
    name  = "character_set_database"
    value = "utf8mb4"
  }
  parameter {
    name  = "collation_connection"
    value = "utf8mb4_unicode_ci"
  }
  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }
}

resource "aws_rds_cluster" "transaction-manager-aurora-cluster" {
  engine                    = "aurora"
  engine_version            = "5.6.10a"
  port                      = "3306"
  cluster_identifier        = "${var.service_name}-aurora-cluster"
  database_name             = "transactionmanager"
  master_username           = "${var.db_user}"
  master_password           = "${random_string.master_password.result}"
  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.service_name}-db-final-snapshot-${md5(timestamp())}"
  backtrack_window          = "${var.backtrack_window}"
  backup_retention_period = "${var.backup_retention_period}"
  preferred_backup_window = "${var.preferred_backup_window}"
  preferred_maintenance_window = "${var.preferred_maintenance_window}"
  storage_encrypted = true
  apply_immediately = true
  db_cluster_parameter_group_name = "${aws_rds_cluster_parameter_group.transaction-manager-db-cluster-param-group.id}"
  db_subnet_group_name = "${aws_db_subnet_group.transaction-manager-db-subnet-group.id}"
  vpc_security_group_ids = ["${aws_security_group.transaction-manager-db-sg.id}"]
  tags {
    Name         = "${var.service_name}-aurora-cluster"
    Owner        = "${var.team_name}"
  }
  lifecycle {
    ignore_changes = ["engine-version"]
  }
}


resource "aws_rds_cluster_instance" "transaction-manager-aurora-instance" {
  count                 = "${var.rds_cluster_instance_count}"
  identifier            = "${var.service_name}-aurora-instance-${count.index}"
  cluster_identifier    = "${aws_rds_cluster.transaction-manager-aurora-cluster.id}"
  instance_class        = "${var.db_instance}"
  publicly_accessible   = false
  apply_immediately     = true
  auto_minor_version_upgrade = true
  tags {
    Name         = "${var.service_name}-Aurora-DB-Instance-${count.index}"
  }
  lifecycle {
    create_before_destroy = true
  }
}
