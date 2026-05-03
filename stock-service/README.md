# stock-service

Inventory ve stock reservation yönetiminden sorumludur. SAGA içinde siparişten sonra stok ayırır, ödeme başarısız olursa veya sipariş iptal edilirse ayrılan stoğu geri bırakır.

## API

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `POST` | `/api/inventory` | Admin | Ürün için inventory oluşturur. |
| `PUT` | `/api/inventory/{productId}` | Admin | Available quantity değerini değiştirir. |
| `PATCH` | `/api/inventory/{productId}/adjust` | Admin | Pozitif/negatif delta uygular. |
| `GET` | `/api/inventory/{productId}` | Bearer | Ürün inventory bilgisini döner. |
| `GET` | `/api/inventory?page=0&size=20` | Bearer | Inventory kayıtlarını listeler. |

Inventory oluşturma:

```json
{
  "productId": 19,
  "availableQuantity": 50
}
```

Stok düzeltme:

```json
{
  "delta": -5
}
```

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `inventories` | Ürün bazlı available ve reserved quantity |
| `stock_reservations` | Order/product bazlı reservation kayıtları |

Reservation status değerleri: `RESERVED`, `RELEASED`.

## SAGA Rolü

Dinler:

| Event | Etki |
| --- | --- |
| `OrderCreatedEvent` | Sipariş kalemleri için stok rezerve etmeye çalışır |
| `PaymentFailedEvent` | Daha önce ayrılan stoğu release eder |
| `OrderCancelledEvent` | Ödeme tamamlanmadan iptal edilen siparişin stoğunu bırakır |

Yayınlar:

| Event | Exchange | Routing key |
| --- | --- | --- |
| `StockReservedEvent` | `stock.exchange` | `stock.reserved` |
| `StockFailedEvent` | `stock.exchange` | `stock.failed` |
| `StockReleasedEvent` | `stock.exchange` | `stock.released` |

## Not

`availableQuantity` ve `reservedQuantity` ayrı tutulur. Bu yapı checkout sırasında ürünü geçici olarak ayırmayı sağlar; ödeme başarısız olursa reserved stok tekrar available alana döner.
