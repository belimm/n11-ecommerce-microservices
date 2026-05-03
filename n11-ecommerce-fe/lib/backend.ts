import { cookies } from "next/headers";
import { getSession } from "@/lib/session";

type BackendOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  auth?: boolean;
  language?: string;
  cache?: RequestCache;
};

export class BackendError extends Error {
  constructor(
    message: string,
    public status: number,
  ) {
    super(message);
  }
}

export function backendBaseUrl() {
  return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
}

export async function currentLanguage(fallback = "tr") {
  const cookieStore = await cookies();
  return cookieStore.get("n11_language")?.value ?? fallback;
}

export async function backendFetch<T>(path: string, options: BackendOptions = {}): Promise<T> {
  const headers = new Headers();
  headers.set("Content-Type", "application/json");
  headers.set("Accept-Language", options.language ?? (await currentLanguage()));

  if (options.auth) {
    const session = await getSession();
    if (!session) throw new BackendError("Authentication required", 401);
    headers.set("Authorization", `Bearer ${session.accessToken}`);
  }

  const method = options.method ?? "GET";
  const url = `${backendBaseUrl()}${path}`;
  let response: Response;

  try {
    response = await fetch(url, {
      method,
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
      cache: options.cache ?? "no-store",
    });
  } catch (error) {
    const detail = error instanceof Error ? error.message : "Unknown network error";
    const message = `Cannot reach backend at ${url}. ${detail}`;
    console.error("Backend request failed before response", { method, url, message });
    throw new BackendError(message, 0);
  }

  if (!response.ok) {
    let message = `${method} ${url} failed with status ${response.status}`;
    try {
      const data = (await response.json()) as { message?: string; error?: string };
      message = data.message ?? data.error ?? message;
    } catch {
      // Keep the URL-rich message when the backend does not return JSON.
    }
    console.error("Backend request failed", { method, url, status: response.status, message });
    throw new BackendError(message, response.status);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function safeBackendFetch<T>(path: string, fallback: T, options: BackendOptions = {}) {
  try {
    return await backendFetch<T>(path, options);
  } catch {
    return fallback;
  }
}

export function toArrayPage<T>(page: { items?: T[]; content?: T[] }) {
  return page.items ?? page.content ?? [];
}
