import Link from "next/link";
import { logoutAction } from "@/app/actions";
import { CartBadge } from "@/components/cart-badge";
import { LanguageSwitcher } from "@/components/language-switcher";
import { currentLanguage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import type { Session } from "@/lib/types";

export async function StoreHeader({ session }: { session: Session | null }) {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).nav;

  return (
    <header className="header">
      <div className="page-shell header-inner">
        <Link href="/" className="brand" aria-label="n11 marketplace home">
          n11<span>market</span>
        </Link>
        <nav className="nav" aria-label="Primary navigation">
          {session?.role === "ADMIN" ? (
            <Link href="/admin/products">{t.admin}</Link>
          ) : (
            <>
              <Link href="/">{t.shop}</Link>
              {session ? (
                <>
                  <Link href="/cart">
                    {t.cart} <CartBadge />
                  </Link>
                  <Link href="/orders">{t.orders}</Link>
                </>
              ) : null}
            </>
          )}
        </nav>
        <div className="nav" aria-label="Account and language">
          <LanguageSwitcher language={language} />
          {session ? (
            <>
              <Link className="profile-pill" href="/profile">
                <span>{session.firstName?.[0] ?? session.username[0]}</span>
                {t.profile}
              </Link>
              <form action={logoutAction}>
                <button type="submit">{t.logout}</button>
              </form>
            </>
          ) : (
            <Link href="/login">{t.login}</Link>
          )}
        </div>
      </div>
    </header>
  );
}
