# EC2 Deployment Guide

This backend is deployed as Docker images built by Jib and run with Docker Compose on EC2. No Dockerfile is required for Spring Boot services.

## 1. EC2 one-time setup

Install Docker and the Docker Compose plugin on the EC2 instance. Create the application directory once:

```bash
sudo mkdir -p /home/ubuntu/ecommerce
sudo chown -R ubuntu:ubuntu /home/ubuntu/ecommerce
```

After this, do not manually copy compose files or `.env.production`. GitHub Actions renders `.env.production` from repository secrets, uploads it to EC2, pulls the latest images, and recreates the containers automatically on every push to `main`.

## 2. GitHub repository secrets

Create these repository secrets in GitHub: Settings -> Secrets and variables -> Actions -> Repository secrets.

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
EC2_HOST
EC2_SSH_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
IYZICO_API_KEY
IYZICO_SECRET_KEY
GRAFANA_ADMIN_USER
GRAFANA_ADMIN_PASSWORD
SLACK_WEBHOOK_URL
```

`EC2_SSH_KEY` must be the private key content used to SSH as `ubuntu`.

Recommended values:

```text
DB_URL=jdbc:postgresql://<rds-endpoint>:5432/postgres?sslmode=require
DB_USERNAME=postgres
RABBITMQ_USERNAME=guest
IYZICO_BASE_URL=https://sandbox-api.iyzipay.com
```

`IYZICO_BASE_URL` is currently fixed in the workflow as the sandbox URL.

## 3. Optional GitHub repository variables

Create these only when needed in Settings -> Secrets and variables -> Actions -> Variables.

```text
PRODUCT_SEED_ENABLED=false
STOCK_SEED_ENABLED=false
```

For the first demo seed on RDS, temporarily set them to `true`, push to `main`, verify the seed data, then set them back to `false` and deploy again.

## 4. Automatic deployment flow

On every push to `main`, `.github/workflows/cd.yml` does this in order:

```text
Checkout
Set up Java 21
Build and push Jib images for each service
Render .env.production from GitHub secrets
Create ~/ecommerce on EC2 if missing
Upload docker-compose.yml, prometheus.yml, .env.production
Docker login on EC2
Docker Compose pull
Docker Compose up -d --remove-orphans
Smoke check api-gateway health
Send Slack notification
```

The service image build order is:

```text
discover-server -> config-server -> api-gateway -> user-service -> product-service -> cart-service -> order-service -> stock-service -> payment-service
```

Runtime startup order is managed by `docker-compose.yml` with `depends_on` and `restart: unless-stopped`.

## 5. Security group

Public or frontend-accessible:

```text
8080 api-gateway
3000 grafana, optional and preferably restricted
```

Restrict to developer IP or private access:

```text
8761 discovery-server
8762 config-server
15672 rabbitmq management
9090 prometheus
3100 loki
```

Do not expose internal service ports `8081-8086` publicly.

## 6. Emergency manual commands

Use these only for debugging on EC2. The normal path is GitHub Actions.

```bash
cd ~/ecommerce
docker compose --env-file .env.production pull
docker compose --env-file .env.production up -d --remove-orphans
docker compose --env-file .env.production ps
curl http://localhost:8080/actuator/health
```

If the older Compose binary is installed instead of the plugin, use `docker-compose`.

## 7. Frontend environment

Vercel frontend should call only the gateway:

```text
NEXT_PUBLIC_API_URL=http://<ec2-public-ip>:8080
NEXT_PUBLIC_GRAFANA_URL=http://<ec2-public-ip>:3000
```

Grafana iframe embedding requires these values, which are already rendered into `.env.production` by the CD workflow:

```text
GF_AUTH_ANONYMOUS_ENABLED=true
GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer
GF_SECURITY_ALLOW_EMBEDDING=true
```
