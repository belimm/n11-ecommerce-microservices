"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { BackendError, backendFetch } from "@/lib/backend";
import { clearSession, getSession, setSession } from "@/lib/session";
import type { Session, User } from "@/lib/types";

function value(formData: FormData, key: string) {
  return String(formData.get(key) ?? "").trim();
}

function optionalValue(formData: FormData, key: string) {
  const raw = value(formData, key);
  return raw.length ? raw : undefined;
}

function numberValue(formData: FormData, key: string, fallback = 0) {
  const parsed = Number(value(formData, key));
  return Number.isFinite(parsed) ? parsed : fallback;
}

function redirectTo(formData: FormData, fallback: string) {
  const target = value(formData, "redirectTo");
  return target.startsWith("/") ? target : fallback;
}

function withErrorParam(target: string, key: string, error: unknown, fallback: string) {
  const message = error instanceof BackendError ? `${error.status}: ${error.message}` : fallback;
  const [pathname, query = ""] = target.split("?");
  const params = new URLSearchParams(query);
  params.set(key, message);
  const serialized = params.toString();
  return serialized ? `${pathname}?${serialized}` : pathname;
}

export async function setLanguageAction(formData: FormData) {
  const language = value(formData, "language") || "tr";
  const cookieStore = await cookies();
  cookieStore.set("n11_language", language, {
    path: "/",
    sameSite: "lax",
    httpOnly: false,
  });
  redirect(redirectTo(formData, "/"));
}

export async function loginAction(formData: FormData) {
  let target = "/";
  try {
    const response = await backendFetch<Session>("/api/auth/signin", {
      method: "POST",
      body: {
        usernameOrEmail: value(formData, "usernameOrEmail"),
        password: value(formData, "password"),
      },
    });
    await setSession(response);
    target = response.role === "ADMIN" ? "/admin/products" : "/";
  } catch (error) {
    const message = error instanceof BackendError ? error.message : "Authentication failed";
    target = `/login?error=${encodeURIComponent(message)}`;
  }
  redirect(target);
}

export async function signupAction(formData: FormData) {
  let target = "/login?registered=true";
  try {
    await backendFetch("/api/auth/signup", {
      method: "POST",
      body: {
        username: value(formData, "username"),
        email: value(formData, "email"),
        password: value(formData, "password"),
        firstName: optionalValue(formData, "firstName"),
        lastName: optionalValue(formData, "lastName"),
        phoneNumber: optionalValue(formData, "phoneNumber"),
      },
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Signup failed";
    target = `/login?error=${encodeURIComponent(message)}`;
  }
  redirect(target);
}

export async function logoutAction() {
  await clearSession();
  redirect("/login");
}

export async function addCartItemAction(formData: FormData) {
  let target = redirectTo(formData, "/cart");
  try {
    await backendFetch("/api/cart/items", {
      method: "POST",
      auth: true,
      body: {
        productId: numberValue(formData, "productId"),
        quantity: numberValue(formData, "quantity", 1),
      },
    });
    revalidatePath("/cart");
  } catch (error) {
    target = withErrorParam(target, "cartError", error, "Unable to add product to cart");
  }
  redirect(target);
}

export async function updateCartItemAction(formData: FormData) {
  const productId = numberValue(formData, "productId");
  await backendFetch(`/api/cart/items/${productId}`, {
    method: "PUT",
    auth: true,
    body: { quantity: numberValue(formData, "quantity", 1) },
  });
  revalidatePath("/cart");
}

export async function removeCartItemAction(formData: FormData) {
  const productId = numberValue(formData, "productId");
  await backendFetch(`/api/cart/items/${productId}`, {
    method: "DELETE",
    auth: true,
  });
  revalidatePath("/cart");
}

export async function clearCartAction() {
  await backendFetch("/api/cart/items", { method: "DELETE", auth: true });
  revalidatePath("/cart");
}


export async function updateProfileAction(formData: FormData) {
  const userId = value(formData, "userId");
  const target = redirectTo(formData, "/profile");
  const updatedUser = await backendFetch<User>(`/api/users/${userId}`, {
    method: "PUT",
    auth: true,
    body: {
      email: value(formData, "email"),
      firstName: optionalValue(formData, "firstName"),
      lastName: optionalValue(formData, "lastName"),
      phoneNumber: optionalValue(formData, "phoneNumber"),
    },
  });
  const session = await getSession();
  if (session) {
    await setSession({
      ...session,
      email: updatedUser.email,
      firstName: updatedUser.firstName,
      lastName: updatedUser.lastName,
    });
  }
  revalidatePath("/profile");
  redirect(`${target}?profileUpdated=true`);
}

export async function changePasswordAction(formData: FormData) {
  const userId = value(formData, "userId");
  let target = redirectTo(formData, "/profile");
  try {
    await backendFetch(`/api/users/${userId}/password`, {
      method: "PATCH",
      auth: true,
      body: {
        currentPassword: value(formData, "currentPassword"),
        newPassword: value(formData, "newPassword"),
      },
    });
    target = `${target}?passwordChanged=true`;
  } catch (error) {
    target = withErrorParam(target, "profileError", error, "Unable to change password");
  }
  redirect(target);
}

export async function updateAddressAction(formData: FormData) {
  const userId = value(formData, "userId");
  const addressId = value(formData, "addressId");
  await backendFetch(`/api/users/${userId}/addresses/${addressId}`, {
    method: "PUT",
    auth: true,
    body: {
      title: value(formData, "title"),
      street: value(formData, "street"),
      city: value(formData, "city"),
      country: value(formData, "country"),
      zipCode: value(formData, "zipCode"),
      defaultAddress: formData.get("defaultAddress") === "on",
    },
  });
  revalidatePath("/profile");
  redirect("/profile?addressUpdated=true");
}

export async function deleteAddressAction(formData: FormData) {
  const userId = value(formData, "userId");
  const addressId = value(formData, "addressId");
  await backendFetch(`/api/users/${userId}/addresses/${addressId}`, {
    method: "DELETE",
    auth: true,
  });
  revalidatePath("/profile");
  redirect("/profile?addressDeleted=true");
}

export async function setDefaultAddressAction(formData: FormData) {
  const userId = value(formData, "userId");
  const addressId = value(formData, "addressId");
  await backendFetch(`/api/users/${userId}/addresses/${addressId}/set-default`, {
    method: "PATCH",
    auth: true,
  });
  revalidatePath("/profile");
  redirect("/profile?addressUpdated=true");
}

export async function createAddressAction(formData: FormData) {
  const userId = value(formData, "userId");
  const address = await backendFetch<{ id?: string }>(`/api/users/${userId}/addresses`, {
    method: "POST",
    auth: true,
    body: {
      title: value(formData, "title"),
      street: value(formData, "street"),
      city: value(formData, "city"),
      country: value(formData, "country"),
      zipCode: value(formData, "zipCode"),
      defaultAddress: formData.get("defaultAddress") === "on",
    },
  });
  revalidatePath("/checkout");
  revalidatePath("/profile");
  const target = redirectTo(formData, "/checkout");
  if (target.startsWith("/profile")) {
    redirect("/profile?addressUpdated=true");
  }
  redirect(address.id ? `${target}?addressId=${encodeURIComponent(address.id)}` : target);
}

export async function createOrderAction(formData: FormData) {
  let target = "/checkout";
  try {
    const order = await backendFetch<{ id: number }>("/api/orders", {
      method: "POST",
      auth: true,
      body: {
        addressId: value(formData, "addressId"),
        paymentMethod: "IYZICO",
        paymentCard: {
          cardHolderName: value(formData, "cardHolderName"),
          cardNumber: value(formData, "cardNumber").replaceAll(" ", ""),
          expireMonth: value(formData, "expireMonth"),
          expireYear: value(formData, "expireYear"),
          cvc: value(formData, "cvc"),
        },
      },
    });
    target = `/orders?created=${order.id}`;
  } catch (error) {
    target = withErrorParam("/checkout", "orderError", error, "Unable to create order");
  }
  redirect(target);
}

export async function cancelOrderAction(formData: FormData) {
  const orderId = numberValue(formData, "orderId");
  await backendFetch(`/api/orders/${orderId}/cancel`, {
    method: "PATCH",
    auth: true,
  });
  revalidatePath("/orders");
}

export async function createProductAction(formData: FormData) {
  await backendFetch("/api/products", {
    method: "POST",
    auth: true,
    body: {
      name: value(formData, "name"),
      slug: value(formData, "slug"),
      description: value(formData, "description"),
      price: value(formData, "price"),
      imageUrl: value(formData, "imageUrl"),
      active: formData.get("active") === "on",
      categorySlug: value(formData, "categorySlug"),
    },
  });
  revalidatePath("/admin/products");
}

export async function deleteProductAction(formData: FormData) {
  await backendFetch(`/api/products/${numberValue(formData, "id")}`, {
    method: "DELETE",
    auth: true,
  });
  revalidatePath("/admin/products");
}

export async function createCategoryAction(formData: FormData) {
  await backendFetch("/api/categories", {
    method: "POST",
    auth: true,
    body: {
      name: value(formData, "name"),
      slug: value(formData, "slug"),
      description: optionalValue(formData, "description"),
    },
  });
  revalidatePath("/admin/categories");
}

export async function deleteCategoryAction(formData: FormData) {
  await backendFetch(`/api/categories/${numberValue(formData, "id")}`, {
    method: "DELETE",
    auth: true,
  });
  revalidatePath("/admin/categories");
}

export async function updateOrderStatusAction(formData: FormData) {
  const orderId = numberValue(formData, "orderId");
  await backendFetch(`/api/orders/${orderId}/status`, {
    method: "PATCH",
    auth: true,
    body: { status: value(formData, "status") },
  });
  revalidatePath("/admin/orders");
}

export async function updateUserStatusAction(formData: FormData) {
  const userId = value(formData, "userId");
  const action = value(formData, "userAction");
  await backendFetch(`/api/users/${userId}/${action}`, {
    method: "PATCH",
    auth: true,
  });
  revalidatePath("/admin/customers");
}

export async function deleteUserAction(formData: FormData) {
  await backendFetch(`/api/users/${value(formData, "userId")}`, {
    method: "DELETE",
    auth: true,
  });
  revalidatePath("/admin/customers");
}

export async function createInventoryAction(formData: FormData) {
  await backendFetch("/api/inventory", {
    method: "POST",
    auth: true,
    body: {
      productId: numberValue(formData, "productId"),
      availableQuantity: numberValue(formData, "availableQuantity"),
    },
  });
  revalidatePath("/admin/inventory");
}

export async function adjustInventoryAction(formData: FormData) {
  const productId = numberValue(formData, "productId");
  await backendFetch(`/api/inventory/${productId}/adjust`, {
    method: "PATCH",
    auth: true,
    body: { delta: numberValue(formData, "delta") },
  });
  revalidatePath("/admin/inventory");
}
