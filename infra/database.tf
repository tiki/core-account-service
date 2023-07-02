# Copyright (c) TIKI Inc.
# MIT license. See LICENSE file in root directory.

resource "digitalocean_database_cluster" "db-cluster-account" {
  name                 = "account-db-cluster-${local.region}"
  engine               = "pg"
  version              = "15"
  size                 = "db-s-1vcpu-1gb"
  region               = local.region
  node_count           = 1
  private_network_uuid = local.vpc_uuid
}

resource "digitalocean_database_db" "db-account" {
  cluster_id = digitalocean_database_cluster.db-cluster-account.id
  name       = "account_service"
}

resource "digitalocean_database_firewall" "db-cluster-l0-auth-fw" {
  cluster_id = digitalocean_database_cluster.db-cluster-account.id

  rule {
    type  = "app"
    value = digitalocean_app.account-app.id
  }

  rule {
    type  = "ip_addr"
    value = "52.4.198.118"
  }
}

resource "digitalocean_database_user" "db-user-account" {
  cluster_id = digitalocean_database_cluster.db-cluster-account.id
  name       = "account-service"
}
