# Copyright (c) TIKI Inc.
# MIT license. See LICENSE file in root directory.

resource "digitalocean_database_cluster" "db-cluster-l0-auth" {
  name                 = "l0-auth-db-cluster-${local.region}"
  engine               = "pg"
  version              = "15"
  size                 = "db-s-1vcpu-1gb"
  region               = local.region
  node_count           = 1
  private_network_uuid = local.vpc_uuid
}

resource "digitalocean_database_db" "db-l0-auth" {
  cluster_id = digitalocean_database_cluster.db-cluster-l0-auth.id
  name       = "l0_auth"
}

resource "digitalocean_database_firewall" "db-cluster-l0-auth-fw" {
  cluster_id = digitalocean_database_cluster.db-cluster-l0-auth.id

  rule {
    type  = "app"
    value = digitalocean_app.l0-auth-app.id
  }
}

resource "digitalocean_database_user" "db-user-l0-auth" {
  cluster_id = digitalocean_database_cluster.db-cluster-l0-auth.id
  name       = "l0-auth-service"
}
