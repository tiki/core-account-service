# Copyright (c) TIKI Inc.
# MIT license. See LICENSE file in root directory.

resource "digitalocean_app" "account-app" {
  spec {
    name   = "account"
    region = local.region

    domain {
      name = "account.mytiki.com"
      type = "PRIMARY"
    }

    service {
      name               = "account-service"
      instance_count     = 2
      instance_size_slug = "professional-xs"
      http_port          = local.port

      image {
        registry_type = "DOCR"
        registry      = "tiki"
        repository    = "account"
        tag           = var.sem_ver
      }

      env {
        type  = "SECRET"
        key   = "DOPPLER_TOKEN"
        value = var.doppler_st
      }

      health_check {
        http_path = "/health"
      }

      alert {
        rule     = "CPU_UTILIZATION"
        value    = 70
        operator = "GREATER_THAN"
        window   = "THIRTY_MINUTES"
      }

      alert {
        rule     = "MEM_UTILIZATION"
        value    = 80
        operator = "GREATER_THAN"
        window   = "TEN_MINUTES"
      }

      alert {
        rule     = "RESTART_COUNT"
        value    = 3
        operator = "GREATER_THAN"
        window   = "TEN_MINUTES"
      }
    }

    alert {
      rule = "DEPLOYMENT_FAILED"
    }

    alert {
      rule = "DOMAIN_FAILED"
    }
  }
}
