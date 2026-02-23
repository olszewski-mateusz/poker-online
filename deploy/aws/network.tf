locals {
  base_cidr = "10.0.0.0/16"
  az_map    = { for i, az in sort(data.aws_availability_zones.available_azs.names) : az => i }
}

data "aws_availability_zones" "available_azs" {
  state = "available"
}

resource "aws_vpc" "main" {
  cidr_block = local.base_cidr
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


resource "aws_nat_gateway" "nat" {
  availability_mode = "regional"
  vpc_id = aws_vpc.main.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block        = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }
}

resource "aws_route_table_association" "private" {
  for_each       = local.az_map
  subnet_id      = aws_subnet.private[each.key].id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "public" {
  description = "Security group allowing traffic on ports 443 and 80"
  name        = "public-http-traffic"
  vpc_id      = aws_vpc.main.id
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  security_group_id = aws_security_group.public.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  security_group_id = aws_security_group.public.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 443
  to_port           = 443
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "public_ingress_same_sg" {
  security_group_id            = aws_security_group.public.id
  referenced_security_group_id = aws_security_group.public.id
  ip_protocol                  = "-1"
}

resource "aws_vpc_security_group_egress_rule" "public_egress" {
  security_group_id = aws_security_group.public.id
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
  referenced_security_group_id = aws_security_group.public.id
  ip_protocol                  = "-1"
}

resource "aws_vpc_security_group_egress_rule" "private_egress" {
  security_group_id = aws_security_group.private.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}
