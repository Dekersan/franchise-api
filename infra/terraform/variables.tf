variable "atlas_org_id" {
  description = "ID de la organizacion de MongoDB Atlas"
  type        = string
}

variable "project_name" {
  description = "Nombre del proyecto en Atlas"
  type        = string
  default     = "franchise-api"
}

variable "cluster_name" {
  description = "Nombre del cluster"
  type        = string
  default     = "franchise-cluster"
}

variable "region" {
  description = "Region de AWS del cluster gratuito"
  type        = string
  default     = "US_EAST_1"
}

variable "database_name" {
  description = "Base de datos que usa la aplicacion"
  type        = string
  default     = "franchises"
}

variable "db_username" {
  description = "Usuario de base de datos de la aplicacion"
  type        = string
  default     = "franchise_app"
}

variable "allowed_ip_addresses" {
  description = "IPs autorizadas para conectarse (VPS y equipo local)"
  type        = list(string)
}