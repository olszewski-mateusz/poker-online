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

  availability_zone_rebalancing = "ENABLED"
  force_new_deployment = true

  network_configuration {
    security_groups = [aws_security_group.private.id]
    subnets          = [for az in aws_subnet.private : az.id]
    assign_public_ip = false
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
