variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment (prod, staging)"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_name" {
  description = "MySQL database name for pet-api"
  type        = string
  default     = "petdb"
}

variable "db_username" {
  description = "MySQL master username"
  type        = string
  default     = "petapi"
}

variable "db_instance_class" {
  description = "RDS instance type"
  type        = string
  default     = "db.t3.micro"
}

variable "db_multi_az" {
  description = "Enable RDS Multi-AZ deployment"
  type        = bool
  default     = false
}

variable "redis_node_type" {
  description = "ElastiCache Redis node type"
  type        = string
  default     = "cache.t3.micro"
}

variable "pet_api_image" {
  description = "Full ECR image URI for pet-api (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/pet-api:latest)"
  type        = string
  default     = ""
}

variable "analytics_image" {
  description = "Full ECR image URI for analytics-service"
  type        = string
  default     = ""
}

variable "mongodb_uri" {
  description = "MongoDB Atlas connection string (supply via TF_VAR_mongodb_uri or Secrets Manager)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "pet_api_desired_count" {
  description = "Desired number of pet-api ECS tasks"
  type        = number
  default     = 2
}

variable "analytics_desired_count" {
  description = "Desired number of analytics-service ECS tasks"
  type        = number
  default     = 1
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS on the ALB (leave empty to use HTTP only)"
  type        = string
  default     = ""
}
