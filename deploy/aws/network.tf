locals {
  base_cidr = "10.0.0.0/16"
  az_map    = { for i, az in sort(data.aws_availability_zones.available_azs.names) : az => i }
}

data "aws_region" "this" {}

data "aws_availability_zones" "available_azs" {
  state = "available"
}

resource "aws_vpc" "main" {
  cidr_block           = local.base_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "Poker Online"
  }
}

resource "aws_subnet" "public" {
  for_each          = local.az_map
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(local.base_cidr, 4, each.value)
  availability_zone = each.key
  tags = {
    Name = "Poker Online Public ${each.key}"
  }
}

resource "aws_subnet" "private" {
  for_each          = local.az_map
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(local.base_cidr, 4, length(local.az_map) + each.value)
  availability_zone = each.key
  tags = {
    Name = "Poker Online Private ${each.key}"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
}

resource "aws_route_table_association" "public" {
  for_each       = local.az_map
  subnet_id      = aws_subnet.public[each.key].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
}

resource "aws_route_table_association" "private" {
  for_each       = local.az_map
  subnet_id      = aws_subnet.private[each.key].id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "public_alb" {
  description = "Security group allowing traffic on port 443"
  name        = "public-http-traffic"
  vpc_id      = aws_vpc.main.id
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.public_alb.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "public_ingress_same_sg" {
  security_group_id            = aws_security_group.public_alb.id
  referenced_security_group_id = aws_security_group.public_alb.id
  ip_protocol                  = "-1"
}

resource "aws_vpc_security_group_egress_rule" "public_egress" {
  security_group_id = aws_security_group.public_alb.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_security_group" "private" {
  description = "Security group allowing traffic only in the same sg"
  name        = "private-http-traffic"
  vpc_id      = aws_vpc.main.id
}

resource "aws_vpc_security_group_ingress_rule" "private_ingress_same_sg" {
  security_group_id            = aws_security_group.private.id
  referenced_security_group_id = aws_security_group.private.id
  ip_protocol                  = "-1"
}

resource "aws_vpc_security_group_ingress_rule" "private_ingress_public_sg" {
  security_group_id            = aws_security_group.private.id
  referenced_security_group_id = aws_security_group.public_alb.id
  ip_protocol                  = "-1"
}

resource "aws_vpc_security_group_egress_rule" "private_egress" {
  security_group_id = aws_security_group.private.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}


resource "aws_security_group" "vpc_endpoints" {
  description = "Security group for VPC interface endpoints"
  name        = "vpc-endpoints"
  vpc_id      = aws_vpc.main.id
}

resource "aws_vpc_security_group_ingress_rule" "vpc_endpoints_https_from_private" {
  security_group_id            = aws_security_group.vpc_endpoints.id
  referenced_security_group_id = aws_security_group.private.id
  from_port                    = 443
  to_port                      = 443
  ip_protocol                  = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "vpc_endpoints_egress" {
  security_group_id = aws_security_group.vpc_endpoints.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${data.aws_region.this.id}.ecr.api"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [for az in aws_subnet.private : az.id]
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true
}

resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${data.aws_region.this.id}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [for az in aws_subnet.private : az.id]
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true
}

resource "aws_vpc_endpoint" "logs" {
  vpc_id              = aws_vpc.main.id
  service_name        = "com.amazonaws.${data.aws_region.this.id}.logs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = [for az in aws_subnet.private : az.id]
  security_group_ids  = [aws_security_group.vpc_endpoints.id]
  private_dns_enabled = true
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = aws_vpc.main.id
  service_name      = "com.amazonaws.${data.aws_region.this.id}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [aws_route_table.private.id]
}
