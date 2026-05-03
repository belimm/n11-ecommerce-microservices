# api-gateway

Backend'in public giriş noktasıdır. Frontend sadece bu servisi çağırmalıdır; domain servislerine doğrudan gitmek local debug dışında tercih edilmez.

## Route Haritası


| Path | Hedef servis |
| --- | --- |
| `/api/auth/**` | `user-service` |
| `/api/users/**` | `user-service` |
| `/api/products/**` | `product-service` |
| `/api/categories/**` | `product-service` |
| `/api/cart/**` | `cart-service` |
| `/api/orders/**` | `order-service` |
| `/api/inventory/**` | `stock-service` |
| `/api/payments/**` | `payment-service` |

Swagger doküman route'ları:

```text
/user-service/v3/api-docs
/product-service/v3/api-docs
/cart-service/v3/api-docs
/order-service/v3/api-docs
/stock-service/v3/api-docs
/payment-service/v3/api-docs
```

## Runtime

| Port | Config |
| ---: | --- |
| 8080 | `API_GATEWAY_PORT` |

Local:

```text
SPRING_PROFILES_ACTIVE=local
CONFIG_URI=http://localhost:8762
```

Docker/prod:

```text
SPRING_PROFILES_ACTIVE=prod,docker
CONFIG_URI=http://config-server:8762
```

## Not

Route'lar Eureka üzerinden load-balanced service name ile çözülür. Bir route `502` dönüyorsa önce Eureka registration, sonra ilgili servisin health/log çıktısı kontrol edilmelidir.
