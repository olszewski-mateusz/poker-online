data "aws_ecr_image" "server_image" {
  repository_name = "poker/server"
  image_tag       = "latest"
}

resource "aws_lb_target_group" "poker-server-target-group" {
  name        = "poker-server-target-group"
  port        = 80 // required, but not used
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id = aws_vpc.main.id

  target_group_health {
    dns_failover {
      minimum_healthy_targets_count      = "1"
      minimum_healthy_targets_percentage = "off"
    }

    unhealthy_state_routing {
      minimum_healthy_targets_count      = "1"
      minimum_healthy_targets_percentage = "off"
    }
  }

  health_check {
    protocol = "HTTP"
    path = "/actuator/health"
  }
}

resource "aws_ecs_service" "poker-server" {
  name            = "poker-server"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = aws_ecs_task_definition.server.arn

  desired_count   = 1
  launch_type = "FARGATE"

  load_balancer {
    target_group_arn = aws_lb_target_group.poker-server-target-group.arn
    container_name   = "poker-server"
    container_port   = 8080
  }

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
  }
}

resource "aws_ecs_task_definition" "server" {
  family = "poker2-server"

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
      name = "poker-server"
      image = data.aws_ecr_image.server_image.image_uri
      essential   = true
      portMappings = [{
        appProtocol   = "http"
        containerPort = 8080
        hostPort      = 8080
        name          = "poker-server-8080-tcp"
        protocol      = "tcp"
      }]
      memory            = 1024
      environment      = [
        {
          name  = "CORS_ALLOWED_HOST"
          value = "https://poker.olszewski.io"
        },
        {
          name  = "REDIS_HOST"
          value = "redis.poker"
        },
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "prod"
        },
        {
          name  = "THC_PATH"
          value = "/actuator/health"
        },
        {
          name  = "THC_PORT"
          value = "8080"
        }
      ]
      healthCheck = { // todo: no need for container health check
        command = [
          "CMD",
          "/workspace/health-check",
        ]
        interval = 30
        retries  = 3
        timeout  = 5
      }
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-create-group  = "true"
          awslogs-group         = "/ecs/poker-server"
          awslogs-region        = "eu-central-1"
          awslogs-stream-prefix = "ecs"
          max-buffer-size       = "25m"
          mode                  = "non-blocking"
        }
      }
    }]
  )
}
