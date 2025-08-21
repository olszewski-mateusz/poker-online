terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 6.9.0"
    }
  }
}

provider "aws" {
  region = "eu-central-1"
}

resource "aws_ecs_cluster" "poker_cluster" {
  name = "poker-online"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Project = "poker-online"
  }
}

resource "aws_ecs_service" "poker-app" {
  name            = "poker-app"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = "arn:aws:ecs:eu-central-1:108782071445:task-definition/poker-app:14"

  desired_count   = 1
  launch_type = "FARGATE"

  load_balancer {
    target_group_arn = aws_lb_target_group.poker-app-target-group.arn
    container_name   = "poker-app"
    container_port   = 80
  }

  network_configuration {
    subnets = [
      "subnet-08db0a05a64550cac",
      "subnet-0437151ed5a78d59e",
      "subnet-0d4f86e8c159691d2"
    ]
    assign_public_ip = true // todo: create nat later?
  }

  tags = {
    Project = "poker-online"
  }
}

resource "aws_ecs_service" "poker-server" {
  name            = "poker-server"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = "arn:aws:ecs:eu-central-1:108782071445:task-definition/poker-server:18"

  desired_count   = 1
  launch_type = "FARGATE"

  load_balancer {
    target_group_arn = aws_lb_target_group.poker-server-target-group.arn
    container_name   = "poker-server"
    container_port   = 8080
  }

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
  }

  tags = {
    Project = "poker-online"
  }
}

resource "aws_ecs_service" "poker-redis" {
  name            = "poker-redis"
  cluster         = aws_ecs_cluster.poker_cluster.id
  task_definition = "arn:aws:ecs:eu-central-1:108782071445:task-definition/poker-redis:6"

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

  tags = {
    Project = "poker-online"
  }
}

resource "aws_lb" "poker-alb" {
  name               = "poker-alb"
  internal           = false
  load_balancer_type = "application"
  subnets = [
    "subnet-08db0a05a64550cac",
    "subnet-0437151ed5a78d59e",
    "subnet-0d4f86e8c159691d2"
  ]

  tags = {
    Project = "poker-online"
  }
}

resource "aws_lb_target_group" "poker-app-target-group" {
  name        = "poker-app-target-group"
  port        = 80 // why do we need port?
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id = "vpc-0ab0997a500618150"

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

  tags = {
    Project = "poker-online"
  }
}

resource "aws_lb_target_group" "poker-server-target-group" {
  name        = "poker-server-target-group"
  port        = 80 // why do we need port?
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id = "vpc-0ab0997a500618150"

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

  tags = {
    Project = "poker-online"
  }
}

resource "aws_lb_listener" "poker-alb-listener" {
  load_balancer_arn = aws_lb.poker-alb.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = "arn:aws:acm:eu-central-1:108782071445:certificate/35a2c24c-0765-4cd0-a756-76a690cdbaa7" // todo: make this as variable

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.poker-app-target-group.arn
  }

  tags = {
    Project = "poker-online"
  }
}

resource "aws_lb_listener_rule" "poker-alb-listener-api-rule" {
  listener_arn = aws_lb_listener.poker-alb-listener.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.poker-server-target-group.arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }

  tags = {
    Project = "poker-online"
  }
}

resource "aws_service_discovery_http_namespace" "poker-namespace" {
  name        = "poker"
  description = "Poker online namespace"

  tags = {
    Project = "poker-online"
  }
}
