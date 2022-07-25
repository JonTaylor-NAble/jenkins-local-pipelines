terraform {
  required_providers {
    # This is the provider mentioned and recognizes docker resources
    docker = {
      source = "kreuzwerker/docker"
      version = "~> 2.13.0"
    }
  }
}

# Don't need to modify the provider settings
provider "docker" {}

# This is the container image from the public docker hub
resource "docker_image" "nginx" {
  name         = "nginx:latest"
  keep_locally = false
}

# This is the container that we want to run
# note that it references the image defined above, this is a taste of how powerful terraform becomes as you scale out.
resource "docker_container" "nginx" {
  image = docker_image.nginx.latest
  name  = "nginx-tf"
  ports {
    internal = 80
    external = 8000
  }
}