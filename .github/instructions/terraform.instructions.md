---
applyTo: "1002-arl-manager_tf/**"
---

# Reglas Cloud & IaC: Terraform y Azure AKS

## Stack Gestionado (Alineado a `docs/03-diseno-tecnico.md` Sección 6)
- **Recursos Azure (Terraform):** Azure Database for PostgreSQL 16 (*Flexible Server*), Azure Key Vault, Azure Container Registry (ACR) y red privada (VNet).
- **Orquestación (AKS / Helm):** Manifiestos Kubernetes con *Secrets Store CSI Driver* (inyectando secretos desde Key Vault), *liveness/readiness probes* apuntando a `/actuator/health` y HPA configurado para soportar hasta 250 usuarios concurrentes.
- **Seguridad:** Prohibido dejar credenciales, cadenas de conexión o secretos en texto plano dentro de `.tf`, `.tfvars` o `values.yaml`.

## Verificación y Control
- **Comandos de validación:**
  `terraform -chdir=1002-arl-manager_tf fmt -check && terraform -chdir=1002-arl-manager_tf validate`
- **Restricción crítica:** Nunca ejecutes `terraform apply`, `terraform destroy` ni `kubectl apply` sin aprobación explícita en el Gate Human-in-the-loop.