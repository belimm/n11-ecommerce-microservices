import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import type { PageResponse, Payment } from "@/lib/types";

export default async function AdminPaymentsPage() {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin.payments;
  const page = await safeBackendFetch<PageResponse<Payment>>("/api/payments?page=0&size=50", {
    content: [],
    page: 0,
    size: 50,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  }, { auth: true });
  const payments = toArrayPage(page);

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.order}</th><th>{t.status}</th><th>{t.provider}</th><th>{t.amount}</th><th>{t.failure}</th></tr></thead>
          <tbody>
            {payments.map((payment) => (
              <tr key={payment.id}>
                <td>{payment.orderNumber}<br /><span className="muted">#{payment.orderId}</span></td>
                <td><span className="status">{payment.status}</span></td>
                <td>{payment.iyzicoPaymentId ?? payment.iyzicoStatus ?? "-"}</td>
                <td>{payment.paidPrice} {payment.currency}</td>
                <td>{payment.failureReason ?? "-"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
