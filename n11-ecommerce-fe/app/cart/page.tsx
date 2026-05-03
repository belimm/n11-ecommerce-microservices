/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { clearCartAction, removeCartItemAction, updateCartItemAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { requireSession } from "@/lib/session";
import type { Cart } from "@/lib/types";

const emptyCart: Cart = {
  id: 0,
  userId: "",
  status: "ACTIVE",
  items: [],
  totalPrice: "0",
};

type CartPageProps = {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export default async function CartPage({ searchParams }: CartPageProps) {
  await requireSession();
  const params = await searchParams;
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).cart;
  const cartError = firstParam(params.cartError);
  const cart = await safeBackendFetch<Cart>("/api/cart", emptyCart, { auth: true });

  return (
    <div className="page-shell section">
      <div className="toolbar">
        <h1 className="display text-5xl">{t.title}</h1>
        <form action={clearCartAction}>
          <button className="btn ghost" type="submit">
            {t.clear}
          </button>
        </form>
      </div>

      {cartError ? (
        <div className="notice error">
          <strong>{t.cartError}</strong>
          <span>{cartError}</span>
        </div>
      ) : null}

      {cart.items.length === 0 ? (
        <div className="panel p-8">
          <h2 className="display text-4xl">{t.emptyTitle}</h2>
          <p className="muted mt-2">{t.emptyCopy}</p>
          <Link className="btn primary mt-6" href="/">
            {t.browse}
          </Link>
        </div>
      ) : (
        <div className="split">
          <div className="stack">
            {cart.items.map((item) => (
              <article className="panel grid gap-4 p-4 md:grid-cols-[120px_1fr_auto]" key={item.id}>
                <img className="aspect-square w-full rounded-lg object-cover" src={item.productImageUrl} alt={item.productName} />
                <div>
                  <h2 className="display text-3xl">{item.productName}</h2>
                  <p className="muted">{item.unitPrice} TL {t.each}</p>
                  <p className="price mt-2">{item.lineTotal} TL</p>
                </div>
                <div className="stack">
                  <form action={updateCartItemAction} className="flex gap-2">
                    <input type="hidden" name="productId" value={item.productId} />
                    <input className="input w-24" name="quantity" type="number" min="1" defaultValue={item.quantity} />
                    <button className="btn" type="submit">
                      {t.update}
                    </button>
                  </form>
                  <form action={removeCartItemAction}>
                    <input type="hidden" name="productId" value={item.productId} />
                    <button className="btn ghost w-full" type="submit">
                      {t.remove}
                    </button>
                  </form>
                </div>
              </article>
            ))}
          </div>
          <aside className="panel p-6">
            <p className="muted">{t.total}</p>
            <p className="display mt-2 text-5xl">{cart.totalPrice} TL</p>
            <Link className="btn primary mt-6 w-full" href="/checkout">
              {t.checkout}
            </Link>
          </aside>
        </div>
      )}
    </div>
  );
}
