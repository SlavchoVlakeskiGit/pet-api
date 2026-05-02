resource "aws_msk_serverless_cluster" "kafka" {
  cluster_name = "pet-api-kafka"

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.msk.id]
  }

  client_authentication {
    sasl {
      iam {
        enabled = true
      }
    }
  }
}

data "aws_msk_bootstrap_brokers" "kafka" {
  cluster_arn = aws_msk_serverless_cluster.kafka.arn
}
