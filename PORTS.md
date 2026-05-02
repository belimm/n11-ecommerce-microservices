# Local Port Map

Only the API Gateway should be called by the frontend or exposed publicly.
Discovery Server and Config Server are control-plane services and should stay private.

| Service | Port | Visibility |
| --- | ---: | --- |
| discovery-server | 8761 | Private/dev only |
| config-server | 8762 | Private/dev only |
| api-gateway | 8080 | Public edge |
| user-service | 8081 | Private |
| product-service | 8082 | Private |
| cart-service | 8083 | Private |
| order-service | 8084 | Private |
| stock-service | 8085 | Private |
| payment-service | 8086 | Private |
| notification-service | 8087 | Reserved |
| review-service | 8088 | Reserved |

## Frontend

Local frontend calls only the gateway:

```text
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## IntelliJ Run Configs

Business services usually only need:

```text
SPRING_PROFILES_ACTIVE=local
CONFIG_URI=http://localhost:8762
```

For IntelliJ services that should use AWS/RDS but still call other services on local
ports, keep Config Server and JVM services on `local` or `prod` without `docker`, and set
`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` on Config Server. Do not set
`CART_SERVICE_BASE_URL`, `PRODUCT_SERVICE_BASE_URL`, or `USER_SERVICE_BASE_URL` to Docker
hostnames while running from IntelliJ.

For Docker Compose or EC2 container networking, use:

```text
SPRING_PROFILES_ACTIVE=prod,docker
```

When using one centralized environment on Config Server, put shared DB/Rabbit/JWT values on
`ConfigServerApplication`; individual services can keep only `SPRING_PROFILES_ACTIVE` and
`CONFIG_URI`.
