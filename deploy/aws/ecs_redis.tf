data "aws_ecr_image" "redis_image" {
  repository_name = "poker/redis"
  image_tag       = "latest"
}

resource "aws_ecs_service" "poker-redis" {
  name            = "poker-redis"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = aws_ecs_task_definition.redis.arn

  desired_count   = 1
  launch_type = "FARGATE"

  network_configuration {
    subnets = [
      "subnet-08db0a05a64550cac",
      "subnet-0437151ed5a78d59e",
      "subnet-0d4f86e8c159691d2"
    ]
    assign_public_ip = true // todo: create nat later?
  }

  service_connect_configuration {
    enabled = true
    namespace = aws_service_discovery_http_namespace.poker-namespace.arn
    service {
      port_name = "poker-redis-6379-tcp"
      client_alias {
        port = 6379
        dns_name = "redis.poker"
      }
      discovery_name = "redis"
    }
  }
}

resource "aws_ecs_task_definition" "redis" {
  family = "poker2-redis"

  cpu          = "512"
  memory       = "1024"
  network_mode = "awsvpc"
  runtime_platform {
    cpu_architecture        = "X86_64"
    operating_system_family = "LINUX"
  }

  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = aws_iam_role.role.arn
  task_role_arn            = aws_iam_role.role.arn

  enable_fault_injection = false

  container_definitions = jsonencode(
    [{
      name = "poker-redis"
      image = data.aws_ecr_image.redis_image.image_uri
      essential   = true
      portMappings = [{
        containerPort = 6379
        hostPort      = 6379
        name          = "poker-redis-6379-tcp"
        protocol      = "tcp"
      }]
      memory            = 1024
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-create-group  = "true"
          awslogs-group         = "/ecs/poker-redis"
          awslogs-region        = "eu-central-1"
          awslogs-stream-prefix = "ecs"
          max-buffer-size       = "25m"
          mode                  = "non-blocking"
        }
      }
    }]
  )
}
