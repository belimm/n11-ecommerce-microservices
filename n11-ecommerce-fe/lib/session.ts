import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import type { Role, Session } from "@/lib/types";

const ACCESS_TOKEN = "n11_access_token";
const REFRESH_TOKEN = "n11_refresh_token";
const USER = "n11_user";

function decodeJwtPayload(token: string): Record<string, unknown> {
  const payload = token.split(".")[1];
  if (!payload) return {};
  const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
  try {
    return JSON.parse(Buffer.from(normalized, "base64").toString("utf8")) as Record<string, unknown>;
  } catch {
    return {};
  }
}

export async function getSession(): Promise<Session | null> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(ACCESS_TOKEN)?.value;
  const userCookie = cookieStore.get(USER)?.value;
  if (!accessToken || !userCookie) return null;

  try {
    const stored = JSON.parse(userCookie) as Omit<Session, "accessToken" | "refreshToken">;
    const payload = decodeJwtPayload(accessToken);
    const realmAccess = payload.realm_access as { roles?: Role[] } | undefined;
    const role = stored.role ?? (realmAccess?.roles?.includes("ADMIN") ? "ADMIN" : "CUSTOMER");
    return {
      ...stored,
      role,
      accessToken,
      refreshToken: cookieStore.get(REFRESH_TOKEN)?.value,
    };
  } catch {
    return null;
  }
}

export async function requireSession(): Promise<Session> {
  const session = await getSession();
  if (!session) redirect("/login");
  return session;
}

export async function requireAdmin(): Promise<Session> {
  const session = await requireSession();
  if (session.role !== "ADMIN") redirect("/");
  return session;
}

export async function setSession(session: Session) {
  const cookieStore = await cookies();
  const secure = process.env.NODE_ENV === "production";
  cookieStore.set(ACCESS_TOKEN, session.accessToken, {
    httpOnly: true,
    sameSite: "lax",
    secure,
    path: "/",
  });
  if (session.refreshToken) {
    cookieStore.set(REFRESH_TOKEN, session.refreshToken, {
      httpOnly: true,
      sameSite: "lax",
      secure,
      path: "/",
    });
  }
  cookieStore.set(
    USER,
    JSON.stringify({
      id: session.id,
      username: session.username,
      email: session.email,
      role: session.role,
      firstName: session.firstName,
      lastName: session.lastName,
    }),
    {
      httpOnly: true,
      sameSite: "lax",
      secure,
      path: "/",
    },
  );
}

export async function clearSession() {
  const cookieStore = await cookies();
  cookieStore.delete(ACCESS_TOKEN);
  cookieStore.delete(REFRESH_TOKEN);
  cookieStore.delete(USER);
}
