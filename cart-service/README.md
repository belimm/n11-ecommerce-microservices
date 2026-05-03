# cart-service

Authenticated kullanıcının aktif sepetinden sorumludur. Sepete ürün eklenirken `product-service` üzerinden ürün detayı alınır ve ürün adı, görseli, birim fiyatı snapshot olarak saklanır.

## API

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `GET` | `/api/cart` | Bearer | Aktif sepeti döner veya oluşturur. |
| `POST` | `/api/cart/items` | Bearer | Ürünü sepete ekler. Varsa quantity artırılır. |
| `PUT` | `/api/cart/items/{productId}` | Bearer | Ürün quantity değerini değiştirir. |
| `DELETE` | `/api/cart/items/{productId}` | Bearer | Ürünü sepetten kaldırır. |
| `DELETE` | `/api/cart/items` | Bearer | Aktif sepeti temizler. |
| `GET` | `/api/cart/message` | Bearer | Basit servis kontrol endpointi. |

Ürün ekleme:

```json
{
  "productId": 19,
  "quantity": 2
}
```

Quantity güncelleme:

```json
{
  "quantity": 3
}
```

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `carts` | Kullanıcı sepeti, durum ve son aktivite zamanı |
| `cart_items` | Ürün snapshotları ve quantity; cart/product unique |

Sepet status değerleri: `ACTIVE`, `ABANDONED`, `CHECKED_OUT`.

## Event

Scheduled job, uzun süre dokunulmamış sepetleri tarar. `CART_ABANDONED_AFTER_HOURS` değerini aşan sepetler için `AbandonedCartEvent` yayınlanır.

| Exchange | Routing key | Queue |
| --- | --- | --- |
| `cart.exchange` | `abandoned.cart` | `abandoned.cart.queue` |

## Dış Çağrı

Ürün ekleme sırasında `product-service` çağrılır. `Accept-Language` header'ı geldiyse ürün snapshotı o dile göre alınır.
