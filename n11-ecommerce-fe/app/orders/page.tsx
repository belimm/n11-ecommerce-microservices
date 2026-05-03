import { cancelOrderAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { requireSession } from "@/lib/session";
import type { Order, PageResponse } from "@/lib/types";

export default async function OrdersPage() {
  await requireSession();
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).orders;
  const page = await safeBackendFetch<PageResponse<Order>>("/api/orders/me?page=0&size=20", {
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  }, { auth: true });
  const orders = toArrayPage(page);

  return (
    <div className="page-shell section">
      <div className="toolbar">
        <h1 className="display text-5xl">{t.title}</h1>
        <span className="muted">{page.totalElements} {t.records}</span>
      </div>
      <div className="stack">
        {orders.map((order) => (
          <article className="panel p-6" key={order.id}>
            <div className="toolbar m-0">
              <div>
                <p className="status">{order.status}</p>
                <h2 className="display mt-3 text-3xl">{order.orderNumber}</h2>
              </div>
              <p className="price text-2xl">{order.totalPrice} TL</p>
            </div>
            {order.statusReason ? <p className="muted mt-4">{order.statusReason}</p> : null}
            <p className="muted mt-4">{order.shippingAddress?.title} - {order.shippingAddress?.city}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              {order.status === "PENDING" || order.status === "CONFIRMED" ? (
                <form action={cancelOrderAction}>
                  <input type="hidden" name="orderId" value={order.id} />
                  <button className="btn ghost" type="submit">{t.cancel}</button>
                </form>
              ) : null}
            </div>
          </article>
        ))}
        {orders.length === 0 ? <p className="muted">{t.empty}</p> : null}
      </div>
    </div>
  );
}
