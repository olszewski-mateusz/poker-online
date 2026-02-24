resource "aws_lb" "poker-alb" {
  name               = "poker-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = [for az in aws_subnet.public : az.id]
  security_groups = [aws_security_group.public_alb.id]
}

data "aws_acm_certificate" "cert" {
  domain = "olszewski.io"
}

resource "aws_lb_listener" "poker-alb-listener" {
  load_balancer_arn = aws_lb.poker-alb.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = data.aws_acm_certificate.cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.poker-app-target-group.arn
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
}

resource "aws_ecs_cluster" "poker_cluster" {
  name = "poker-online"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_service_discovery_http_namespace" "poker-namespace" {
  name        = "poker"
  description = "Poker online namespace"
}
