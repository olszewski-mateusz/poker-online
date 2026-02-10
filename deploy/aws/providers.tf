terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.9.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "5.11.0"
    }
  }
}

provider "aws" {
  region = "eu-central-1"
  default_tags {
    tags = {
      Project = "Poker Online"
      Managed = "Terraform"
    }
  }
}

provider "cloudflare" {

}
