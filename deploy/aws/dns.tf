data "cloudflare_zone" "example_zone" {
  filter = {
    name = "olszewski.io"
  }
}

output "zone_id" {
  value = data.cloudflare_zone.example_zone.zone_id
}

resource "cloudflare_dns_record" "example_dns_record" {
  zone_id = data.cloudflare_zone.example_zone.zone_id
  name = "poker"
  ttl = 90
  type = "CNAME"
  comment = "Terraform managed - poker online project"
  content = aws_lb.poker-alb.dns_name
  proxied = false
}
