# order-service

Sipariş oluşturma ve sipariş yaşam döngüsünden sorumludur. Kullanıcının aktif sepetinden sipariş üretir, teslimat adresi ve ürün bilgilerini snapshot olarak saklar, sonra `OrderCreatedEvent` yayınlayarak SAGA akışını başlatır.

## API

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `POST` | `/api/orders` | Bearer | Aktif sepetten `PENDING` sipariş oluşturur ve SAGA'yı başlatır. |
| `GET` | `/api/orders/{orderId}` | Bearer | Sahibi veya admin siparişi okuyabilir. |
| `GET` | `/api/orders/me?page=0&size=10` | Bearer | Current user siparişleri. |
| `GET` | `/api/orders?page=0&size=20` | Admin | Tüm siparişler. |
| `PATCH` | `/api/orders/{orderId}/status` | Admin | Manuel status güncelleme. |
| `PATCH` | `/api/orders/{orderId}/cancel` | Bearer | Durum uygunsa müşteri iptali. |

Sipariş oluşturma:

```json
{
  "addressId": "address-uuid",
  "paymentMethod": "IYZICO",
  "paymentCard": {
    "cardHolderName": "John Doe",
    "cardNumber": "5526080000000006",
    "expireMonth": "12",
    "expireYear": "2030",
    "cvc": "123"
  }
}
```

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `orders` | Sipariş no, user id, status, total, payment method, shipping snapshot |
| `order_items` | Ürün snapshotı, birim fiyat, quantity, line total |

Order status değerleri: `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`.

Geçişler:

| From | To |
| --- | --- |
| `PENDING` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `SHIPPED`, `CANCELLED` |
| `SHIPPED` | `DELIVERED` |
| `DELIVERED` | Terminal |
| `CANCELLED` | Terminal |

## SAGA Rolü

Yayınlar:

| Event | Exchange | Routing key |
| --- | --- | --- |
| `OrderCreatedEvent` | `order.exchange` | `order.created` |
| `OrderCancelledEvent` | `order.exchange` | `order.cancelled` |

Dinler:

| Event | Etki |
| --- | --- |
| `PaymentSuccessEvent` | Siparişi `CONFIRMED` yapar |
| `StockFailedEvent` | Yetersiz stok nedeniyle siparişi iptal eder |
| `StockReleasedEvent` | Payment failure compensation sonrası siparişi iptal eder |

## Dış Çağrılar

Aktif sepet `cart-service` üzerinden, seçilen adres `user-service` üzerinden alınır. Adres siparişe gömülü kaydedilir; kullanıcı daha sonra adresini değiştirirse eski sipariş etkilenmez.
