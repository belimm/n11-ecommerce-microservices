# product-service

Kategori, ürün, görsel, fiyat, aktiflik ve lokalize katalog metinlerinden sorumludur. Storefront public endpointleri burayı okur; admin panel ürün ve kategori yönetimini buradan yapar.

## API

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `GET` | `/api/products?page=0&size=12` | Public | Aktif ürünleri listeler. `categorySlug` destekler. |
| `GET` | `/api/products/{id}` | Public | ID ile ürün detayı. |
| `GET` | `/api/products/slug/{slug}` | Public | Slug ile ürün detayı. |
| `POST` | `/api/products` | Admin | Ürün oluşturur. |
| `PUT` | `/api/products/{id}` | Admin | Ürün günceller. Null alanlar yok sayılır. |
| `DELETE` | `/api/products/{id}` | Admin | Ürün siler. |
| `GET` | `/api/categories` | Public | Kategorileri listeler. |
| `GET` | `/api/categories/{id}` | Public | Kategori detayı. |
| `POST` | `/api/categories` | Admin | Kategori oluşturur. |
| `PUT` | `/api/categories/{id}` | Admin | Kategori günceller. |
| `DELETE` | `/api/categories/{id}` | Admin | Ürün bağlı değilse kategoriyi siler. |

Lokalizasyon `Accept-Language` header'ı ile çalışır:

```http
Accept-Language: tr
Accept-Language: en
```

## Örnek Body

Ürün oluşturma:

```json
{
  "name": "Samsung Galaxy S24 Ultra 256 GB",
  "slug": "samsung-galaxy-s24-ultra-256gb",
  "description": "Flagship smartphone with AI camera tools.",
  "price": 64999.00,
  "imageUrl": "https://example.com/s24.jpg",
  "active": true,
  "categorySlug": "electronics",
  "translations": [
    {
      "locale": "tr",
      "name": "Samsung Galaxy S24 Ultra 256 GB",
      "description": "AI kamera özelliklerine sahip amiral gemisi telefon."
    }
  ]
}
```

Kategori oluşturma:

```json
{
  "name": "Electronics",
  "slug": "electronics",
  "description": "Phones, computers and accessories",
  "translations": [
    {
      "locale": "tr",
      "name": "Elektronik",
      "description": "Telefon, bilgisayar ve aksesuarlar"
    }
  ]
}
```

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `categories` | Default kategori alanları ve slug |
| `category_translations` | Locale bazlı kategori adı/açıklaması |
| `products` | Default ürün alanları, fiyat, görsel, aktiflik, kategori ilişkisi |
| `product_translations` | Locale bazlı ürün adı/açıklaması/search text |

## Not

`cart-service`, sepete ürün eklerken ürün detayını buradan okur ve fiyat/ad/görsel snapshotı tutar. Böylece sonradan yapılan fiyat değişikliği eski sepet veya siparişleri sessizce değiştirmez.
