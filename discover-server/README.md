# discover-server

Eureka service registry'dir. Uygulama servisleri buraya register olur; API Gateway de route hedeflerini logical service name ile buradan çözer.

## Runtime

| Port | Amaç |
| ---: | --- |
| 8761 | Eureka dashboard ve registry API |

Local UI:

```text
http://localhost:8761
```

Production UI, security group IP'ine izin veriyorsa:

```text
http://<EC2_PUBLIC_IP>:8761
```

## Beklenen Servisler

Backend tamamen çalışırken Eureka'da şunlar görünmelidir:

```text
API-GATEWAY
CONFIG-SERVER
USER-SERVICE
PRODUCT-SERVICE
CART-SERVICE
ORDER-SERVICE
STOCK-SERVICE
PAYMENT-SERVICE
```

Sadece gateway/config görünüyorsa domain servisleri startup sırasında düşmüş olabilir. Önce servis loglarına, sonra Config Server resolved datasource/RabbitMQ ayarlarına bakmak gerekir.
