resource "aws_security_group" "card_rails_security_group" {

  name = "${var.redis_name}-security-group"
  description = "Security group for controlling access to ${var.redis_name}"
  vpc_id = "${var.vpc_id}"

  ingress {
    from_port = "${var.redis_port}"
    to_port = "${var.redis_port}"
    protocol = "${var.db_protocol}"
    cidr_blocks = ["${var.vpc_cidr_block}", "${var.office_cidr_block}"]
  }

  ingress {
    from_port = 16379
    to_port   = 16379
    protocol  = "${var.db_protocol}"
    cidr_blocks = ["${var.vpc_cidr_block}", "${var.office_cidr_block}"]
  }

  ingress {
    from_port = 26379
    to_port   = 26379
    protocol  = "${var.db_protocol}"
    cidr_blocks = ["${var.vpc_cidr_block}", "${var.office_cidr_block}"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["${var.vpc_cidr_block}"]
  }

  lifecycle {
    ignore_changes = ["name"]
  }

  tags {
    Name = "${var.redis_name}"
    Owner = "${var.team_name}"
  }
}