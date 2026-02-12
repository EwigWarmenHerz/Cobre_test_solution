provider "aws" {
  region                      = "us-east-1"
  access_key                  = "taller" 
  secret_key                  = "taller"
  skip_credentials_validation = true
  skip_requesting_account_id  = true

  endpoints {
    dynamodb = "http://localhost:4566"
    sqs      = "http://localhost:4566"
  }
}

resource "aws_dynamodb_table" "delivery_logs" {
  name         = "DeliveryLogs" # ¡OJO! Mayúsculas cuentan
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "notification_id" # Debe coincidir con item.put("notification_id", ...)
  range_key    = "timestamp"

  attribute {
    name = "notification_id"
    type = "S"
  }

  attribute {
    name = "timestamp"
    type = "S"
  }
}

# 2. SQS - Dead Letter Queue (La "DLE" final para fallos definitivos)
resource "aws_sqs_queue" "notification_dlq" {
  name = "notification-dead-letter-queue"
}

# 3. SQS - Main Queue (La cola principal donde llegan los eventos)
resource "aws_sqs_queue" "notification_event_queue" {
  name = "notification-event-queue"
  
  # Redrive policy para mandar a la DLQ tras X fallos técnicos graves
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = 5
  })
}

# 4. SQS - Retry Queue (La cola con retraso para el backoff)
resource "aws_sqs_queue" "notification_retry_queue" {
  name = "notification-retry-queue"
  
  # Los mensajes en esta cola no son visibles de inmediato (Simula el delay)
  delay_seconds = 300 # 5 minutos de espera antes de que el worker la vuelva a leer
  
  tags = {
    Description = "Queue for backoff retries"
  }
}
