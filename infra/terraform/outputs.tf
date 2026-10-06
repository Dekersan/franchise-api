locals {
  srv_host = trimprefix(mongodbatlas_advanced_cluster.this.connection_strings.standard_srv, "mongodb+srv://")
}

output "project_id" {
  description = "ID del proyecto en Atlas"
  value       = mongodbatlas_project.this.id
}

output "cluster_connection_string" {
  description = "Cadena de conexion del cluster, sin credenciales"
  value       = mongodbatlas_advanced_cluster.this.connection_strings.standard_srv
}

output "mongodb_uri" {
  description = "URI completa para la variable MONGODB_URI de la aplicacion"
  value       = "mongodb+srv://${var.db_username}:${random_password.db.result}@${local.srv_host}/${var.database_name}?retryWrites=true&w=majority"
  sensitive   = true
}