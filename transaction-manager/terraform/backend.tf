// DO NOT MODIFY - OVERWRITTEN AT RUNTIME BY PIPELINE

terraform {
  backend "s3" {
    // can't contain interpolations
    bucket = "10x-devblue-terraform-state"
    key = "services/transaction-manager-terraform/terraform.tfstate"
    region = "eu-west-1"
    dynamodb_table = "terraform-state-lock"
  }
}