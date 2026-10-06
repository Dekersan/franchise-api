resource "mongodbatlas_project" "this" {
  name   = var.project_name
  org_id = var.atlas_org_id
}

resource "mongodbatlas_advanced_cluster" "this" {
  project_id   = mongodbatlas_project.this.id
  name         = var.cluster_name
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = "AWS"
          region_name           = var.region
          priority              = 7
        }
      ]
    }
  ]
}

resource "random_password" "db" {
  length  = 24
  special = false
}

resource "mongodbatlas_database_user" "app" {
  project_id         = mongodbatlas_project.this.id
  username           = var.db_username
  password           = random_password.db.result
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.database_name
  }

  scopes {
    name = mongodbatlas_advanced_cluster.this.name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_project_ip_access_list" "allowed" {
  for_each   = toset(var.allowed_ip_addresses)
  project_id = mongodbatlas_project.this.id
  ip_address = each.value
  comment    = "Acceso autorizado para franchise-api"
}