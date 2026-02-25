data "aws_partition" "current" {}

data "aws_iam_policy_document" "ecs_tasks_assume_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    principals {
      type = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "POKER_ONLINE_ECS_ROLE"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_policy.json
  max_session_duration = 3600
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_managed" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "ecs_task_execution_logs_create_group" {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup"
    ]
    resources = [
      "*"
    ]
  }
}

resource "aws_iam_role_policy" "ecs_task_execution_logs_create_group" {
  name   = "PokerOnlineEcsTaskExecutionLogsCreateGroup"
  policy = data.aws_iam_policy_document.ecs_task_execution_logs_create_group.json
  role   = aws_iam_role.ecs_task_execution_role.name
}

resource "aws_iam_role" "ecs_task_role" {
  name                 = "POKER_ONLINE_ECS_TASK_ROLE"
  assume_role_policy   = data.aws_iam_policy_document.ecs_tasks_assume_policy.json
  max_session_duration = 3600
}
