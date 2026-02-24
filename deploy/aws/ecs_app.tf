data "aws_ecr_image" "app_image" {
  repository_name = "poker/app"
  image_tag       = "latest"
}

resource "aws_lb_target_group" "poker-app-target-group" {
  name        = "poker-app-target-group"
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
    path = "/health"
  }
}

resource "aws_ecs_service" "poker-app" {
  name            = "poker-app"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = aws_ecs_task_definition.app.arn

  desired_count   = 1
  launch_type = "FARGATE"

  load_balancer {
    target_group_arn = aws_lb_target_group.poker-app-target-group.arn
    container_name   = "poker-app"
    container_port   = 80
  }

  availability_zone_rebalancing = "ENABLED"
  force_new_deployment = true

  network_configuration {
    security_groups = [aws_security_group.private.id]
    subnets          = [for az in aws_subnet.private : az.id]
    assign_public_ip = false
  }
}

resource "aws_ecs_task_definition" "app" {
  family = "poker2-app"

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
      name = "poker-app"
      image = data.aws_ecr_image.app_image.image_uri
      essential   = true
      portMappings = [{
        appProtocol   = "http"
        containerPort = 80
        hostPort      = 80
        name          = "poker-app-80-tcp"
        protocol      = "tcp"
      }]
      memory            = 1024
      memoryReservation = 1024
      healthCheck = {
        command = [
          "CMD-SHELL",
          "curl -f http://localhost/health || exit 1",
        ]
        interval = 30
        retries  = 3
        timeout  = 5
      }
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-create-group  = "true"
          awslogs-group         = "/ecs/poker-app"
          awslogs-region        = "eu-central-1"
          awslogs-stream-prefix = "ecs"
          max-buffer-size       = "25m"
          mode                  = "non-blocking"
        }
      }
    }]
  )
}

