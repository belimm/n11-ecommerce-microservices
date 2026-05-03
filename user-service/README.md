# user-service

Authentication, kullanıcı profili, refresh token ve adres yönetiminden sorumludur. Access token servis içinde üretilir; refresh token DB'de tutulduğu için logout veya revoke gibi akışlar yönetilebilir.

## API

Gateway üzerinden base path: `/api`.

| Method | Path | Yetki | Açıklama |
| --- | --- | --- | --- |
| `POST` | `/api/auth/signup` | Public | `CUSTOMER` kullanıcı oluşturur. Request içindeki role dikkate alınmaz. |
| `POST` | `/api/auth/signin` | Public | Username/email ve password ile JWT döner. |
| `POST` | `/api/auth/refresh` | Public | Refresh token ile yeni access token üretir. |
| `POST` | `/api/auth/logout` | `X-User-Id` | Refresh token'ı revoke eder. |
| `GET` | `/api/users` | Admin | Kullanıcıları listeler. |
| `GET` | `/api/users/{id}` | Bearer | Kullanıcı profilini döner. |
| `PUT` | `/api/users/{id}` | Bearer | Email, ad, soyad ve telefonu günceller. |
| `PATCH` | `/api/users/{id}/password` | Bearer | Şifre değiştirir. |
| `DELETE` | `/api/users/{id}` | Admin | Kullanıcı siler. |
| `PATCH` | `/api/users/{id}/activate` | Admin | Kullanıcıyı aktif eder. |
| `PATCH` | `/api/users/{id}/deactivate` | Admin | Kullanıcıyı pasife alır. |
| `POST` | `/api/users/{userId}/addresses` | Bearer | Adres oluşturur. |
| `GET` | `/api/users/{userId}/addresses` | Bearer | Kullanıcının adreslerini listeler. |
| `GET` | `/api/users/{userId}/addresses/default` | Bearer | Varsayılan adresi döner. |
| `PUT` | `/api/users/{userId}/addresses/{addressId}` | Bearer | Adres günceller. |
| `DELETE` | `/api/users/{userId}/addresses/{addressId}` | Bearer | Adres siler. |
| `PATCH` | `/api/users/{userId}/addresses/{addressId}/set-default` | Bearer | Adresi varsayılan yapar. |

## Örnek Body

Signup:

```json
{
  "username": "berk",
  "email": "berk@example.com",
  "password": "secret123",
  "firstName": "Berk",
  "lastName": "Limoncu",
  "phoneNumber": "+905555555555"
}
```

Signin:

```json
{
  "usernameOrEmail": "berk",
  "password": "secret123"
}
```

Adres:

```json
{
  "title": "Home",
  "street": "Example Street No: 10",
  "city": "Istanbul",
  "country": "Turkey",
  "zipCode": "34000",
  "defaultAddress": true
}
```

## Tablolar

| Tablo | Amaç |
| --- | --- |
| `users` | Kullanıcı profili, rol, aktiflik ve password hash |
| `addresses` | Kullanıcı teslimat adresleri ve default flag |
| `refresh_tokens` | Süresi ve revoke durumu tutulan refresh token kayıtları |

## Event

Signup sonrası `UserRegisteredEvent`, `user.exchange` üzerine `user.registered` routing key'i ile yayınlanır. Bu event welcome email akışında kullanılır.
