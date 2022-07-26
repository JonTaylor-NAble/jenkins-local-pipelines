terraform {
  required_providers {
    # This is the provider mentioned and recognizes docker resources
    docker = {
      source = "kreuzwerker/docker"
      version = "~> 2.13.0"
    }
    kustomization = {
      source = "kbst/kustomization"
      version = "0.9.0"
    }
  }
}

provider "kustomization" {
  kubeconfig_path = "~/.kube/config"
}

module "example_custom_manifests" {
  source  = "kbst.xyz/catalog/custom-manifests/kustomization"
  version = "0.3.0"

  configuration_base_key = "default"  # must match workspace name
  configuration = {
    default = {
      namespace = "example-${terraform.workspace}"

      resources = [
        "${path.root}/manifests/example/namespace.yaml"
      ]

      common_labels = {
        "env" = terraform.workspace
      }
    }
  }
}