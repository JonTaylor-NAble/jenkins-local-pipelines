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

module "example_custom_manifests" {
  source  = "kbst.xyz/catalog/custom-manifests/kustomization"
  version = "0.3.0"

  configuration_base_key = "default"  # must match workspace name
  configuration = {
    default = {
      namespace = "example-${terraform.workspace}"

      resources = [
        "${path.root}/manifests/example/namespace.yaml",
        "${path.root}/manifests/example/deployment.yaml",
        "${path.root}/manifests/example/service.yaml"
      ]

      common_labels = {
        "env" = terraform.workspace
      }
    }
  }
}