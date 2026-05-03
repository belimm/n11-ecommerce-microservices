# config-server

Merkezi konfigürasyon servisidir. Uygulama YAML dosyaları `src/main/resources/config` altında tutulur ve Spring Cloud Config native mode ile servis edilir.

## Dosya Yapısı

| Dosya | Amaç |
| --- | --- |
| `application.yml` | Tüm servislerin ortak ayarları |
| `application-local.yml` | Local profile defaultları |
| `application-prod.yml` | Production profile defaultları |
| `application-docker.yml` | Docker network override'ları |
| `{service}.yml` | Servis bazlı temel config |
| `{service}-local.yml` | Local datasource/mail vb. |
| `{service}-prod.yml` | Production datasource/mail vb. |

## Ortak Değerler

Config Server DB, RabbitMQ, Eureka, JWT, Loki, mail ve servis URL ayarlarını dağıtır. Secret değerler YAML içine yazılmamalı, environment variable olarak verilmelidir.

Önemli environment değişkenleri:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
EUREKA_URI
LOKI_URL
IYZICO_API_KEY
IYZICO_SECRET_KEY
MAIL_USERNAME
MAIL_PASSWORD
MAIL_ENABLED
```

## Resolved Config Kontrolü

```bash
curl http://localhost:8762/product-service/local
curl http://localhost:8762/order-service/prod,docker
```

`overrides` property source en yüksek önceliğe sahiptir. Bir serviste config yanlış görünüyorsa önce Config Server endpoint çıktısı kontrol edilmelidir.
