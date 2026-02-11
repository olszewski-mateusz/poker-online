data "aws_iam_policy_document" "power_access" {
  statement {
    effect = "Allow"
    not_actions = [
      "iam:*",
      "organizations:*",
      "account:*"
    ]
    resources = ["*"]
  }

  statement {
    effect = "Allow"
    actions = [
      "account:GetAccountInformation",
      "account:GetGovCloudAccountInformation",
      "account:GetPrimaryEmail",
      "account:ListRegions",
      "iam:CreateServiceLinkedRole",
      "iam:DeleteServiceLinkedRole",
      "iam:ListRoles",
      "organizations:DescribeEffectivePolicy",
      "organizations:DescribeOrganization"
    ]
    resources = ["*"]
  }
}

data "aws_iam_policy_document" "assume_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    effect = "Allow"
    principals {
      type = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "role" {
  name = "POKER_ONLINE_ECS_ROLE"
  assume_role_policy = data.aws_iam_policy_document.assume_policy.json
  max_session_duration = 3600
}

resource "aws_iam_role_policy" "policy" {
  name = "PokerOnlineEcsPolicy"
  policy = data.aws_iam_policy_document.power_access.json
  role   = aws_iam_role.role.name
}
