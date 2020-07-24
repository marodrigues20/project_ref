terraform {
  backend "s3" {
    #bucket = "10x-devblue-terraform-state"
    key    = "services/card-rails/terraform.tfstate"
    region = "eu-west-1"
  }
}