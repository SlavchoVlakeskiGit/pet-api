output "alb_dns_name" {
  description = "Public DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "RDS MySQL endpoint"
  value       = aws_db_instance.mysql.endpoint
  sensitive   = true
}

output "redis_endpoint" {
  description = "ElastiCache Redis primary endpoint"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
  sensitive   = true
}

output "msk_bootstrap_brokers" {
  description = "MSK Serverless bootstrap broker string"
  value       = data.aws_msk_bootstrap_brokers.kafka.bootstrap_brokers_sasl_iam
  sensitive   = true
}

output "pet_api_ecr_url" {
  description = "ECR repository URL for pet-api"
  value       = aws_ecr_repository.pet_api.repository_url
}

output "analytics_ecr_url" {
  description = "ECR repository URL for analytics-service"
  value       = aws_ecr_repository.analytics.repository_url
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}
