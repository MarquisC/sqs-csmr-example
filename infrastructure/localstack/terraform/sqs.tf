
resource "aws_sqs_queue" "terraform_queue" {
  name                      = var.sqs_queue_name
  tags = {
    Environment = "localstack"
  }
}

variable "sqs_queue_name" {
  type = string
  default = "default-queue"
}