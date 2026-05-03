# payment-service

Ödeme kayıtlarından ve Iyzico sandbox entegrasyonundan sorumludur. HTTP üzerinden ödeme başlatmaz; `StockReservedEvent` geldiğinde ödeme adımını çalıştırır, sonucu kaydeder ve SAGA'nın devam eventini yayınlar.

## API

Payment creation event-driven çalışır. HTTP endpointleri lookup amaçlıdır.

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `GET` | `/api/payments/orders/{orderId}` | Bearer | Sahibi veya admin order payment kaydını okuyabilir. |
| `GET` | `/api/payments/me?page=0&size=10` | Bearer | Current user payment kayıtları. |
| `GET` | `/api/payments?page=0&size=20` | Admin | Tüm payment kayıtları. |

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `payments` | Order payment status, Iyzico id, price, currency, failure reason |
| `payment_items` | Reserved order item üzerinden kopyalanan ürün snapshotları |

Payment status değerleri: `PENDING`, `SUCCESS`, `FAILED`.

## SAGA Rolü

Dinler:

| Event | Etki |
| --- | --- |
| `StockReservedEvent` | Iyzico sandbox ödeme çağrısı yapar ve payment kaydı oluşturur/günceller |

Yayınlar:

| Event | Exchange | Routing key |
| --- | --- | --- |
| `PaymentSuccessEvent` | `payment.exchange` | `payment.success` |
| `PaymentFailedEvent` | `payment.exchange` | `payment.failed` |

## Iyzico Config

Config Server/environment üzerinden okunur:

```text
IYZICO_API_KEY
IYZICO_SECRET_KEY
IYZICO_BASE_URL=https://sandbox-api.iyzipay.com
```

Local sandbox denemelerinde default test kartı şu değerlerle override edilebilir: `IYZICO_CARD_NUMBER`, `IYZICO_CARD_EXPIRE_MONTH`, `IYZICO_CARD_EXPIRE_YEAR`, `IYZICO_CARD_CVC`.

## Kalan Küçük İş

Order cancellation artık payment-service tarafından da dinlenir. Başarılı Iyzico ödemelerinde `paymentId` ile `/payment/cancel` çağrısı yapılır; ödeme yakalanmadan önceki veya başarısız ödemelerde provider'a gereksiz çağrı yapılmadan lokal payment state kapatılır.
