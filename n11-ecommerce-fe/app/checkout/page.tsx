import { CheckoutOrderForm } from "@/components/checkout-order-form";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage, testCards } from "@/lib/i18n";
import { requireSession } from "@/lib/session";
import type { Address, Cart } from "@/lib/types";

const emptyCart: Cart = {
  id: 0,
  userId: "",
  status: "ACTIVE",
  items: [],
  totalPrice: "0",
};

type CheckoutPageProps = {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}


export default async function CheckoutPage({ searchParams }: CheckoutPageProps) {
  const params = await searchParams;
  const orderError = firstParam(params.orderError);
  const selectedAddressId = firstParam(params.addressId);
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).checkout;
  const session = await requireSession();
  const [cart, addresses] = await Promise.all([
    safeBackendFetch<Cart>("/api/cart", emptyCart, { auth: true }),
    safeBackendFetch<Address[]>(`/api/users/${session.id}/addresses`, [], { auth: true }),
  ]);

  return (
    <div className="page-shell section">
      <h1 className="display text-5xl">{t.title}</h1>
      {orderError ? (
        <div className="notice error">
          <strong>{t.orderError}</strong>
          <span>{orderError}</span>
        </div>
      ) : null}
      <div className="split mt-8">
        <section className="stack">
          <CheckoutOrderForm
            addresses={addresses}
            hasCartItems={cart.items.length > 0}
            initialAddressId={selectedAddressId}
            language={language}
            sessionId={session.id}
            testCards={testCards}
            text={t}
          />
        </section>

        <aside className="panel p-6">
          <p className="muted">{t.method}</p>
          <h2 className="display mt-2 text-4xl">{t.iyzico}</h2>
          <p className="muted mt-4">{t.copy}</p>
          <p className="price mt-8 text-3xl">{cart.totalPrice} TL</p>
        </aside>
      </div>
    </div>
  );
}
