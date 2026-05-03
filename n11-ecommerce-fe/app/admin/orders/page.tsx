import { updateOrderStatusAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import type { Order, PageResponse } from "@/lib/types";

export default async function AdminOrdersPage() {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin.orders;
  const page = await safeBackendFetch<PageResponse<Order>>("/api/orders?page=0&size=50", {
    content: [],
    page: 0,
    size: 50,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  }, { auth: true });
  const orders = toArrayPage(page);

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.order}</th><th>{t.customer}</th><th>{t.total}</th><th>{t.status}</th><th>{t.reason}</th><th>{t.update}</th></tr></thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>{order.orderNumber}</td>
                <td>{order.userId}</td>
                <td>{order.totalPrice} TL</td>
                <td><span className="status">{order.status}</span></td>
                <td className="muted">{order.statusReason ?? "-"}</td>
                <td>
                  <form action={updateOrderStatusAction} className="flex gap-2">
                    <input type="hidden" name="orderId" value={order.id} />
                    <select className="input" name="status" defaultValue={order.status}>
                      {["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"].map((status) => <option key={status}>{status}</option>)}
                    </select>
                    <button className="btn" type="submit">{t.save}</button>
                  </form>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
