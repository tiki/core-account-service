# Copyright (c) TIKI Inc.
# MIT license. See LICENSE file in root directory.

terraform {
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
  }

  backend "remote" {
    organization = "tiki"

    workspaces {
      name = "account"
    }
  }
}

variable "do_pat" {}

provider "digitalocean" {
  token = var.do_pat
}
