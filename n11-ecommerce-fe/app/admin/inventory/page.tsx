import Link from "next/link";
import { adjustInventoryAction, createInventoryAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import type { Inventory, PageResponse } from "@/lib/types";

export default async function AdminInventoryPage() {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin.inventory;
  const page = await safeBackendFetch<PageResponse<Inventory>>("/api/inventory?page=0&size=50", {
    content: [],
    page: 0,
    size: 50,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  }, { auth: true });
  const inventories = toArrayPage(page);

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <section className="panel p-6">
        <h2 className="display text-3xl">{t.createTitle}</h2>
        <form action={createInventoryAction} className="form-grid mt-5">
          <label className="field"><span>{t.productId}</span><input className="input" name="productId" type="number" required /></label>
          <label className="field"><span>{t.availableQuantity}</span><input className="input" name="availableQuantity" type="number" min="0" required /></label>
          <button className="btn primary" type="submit">{t.create}</button>
        </form>
      </section>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.product}</th><th>{t.available}</th><th>{t.reserved}</th><th>{t.adjust}</th></tr></thead>
          <tbody>
            {inventories.map((inventory) => (
              <tr key={inventory.id}>
                <td><Link className="table-link" href={`/products/${inventory.productId}`}>#{inventory.productId}</Link></td>
                <td>{inventory.availableQuantity}</td>
                <td>{inventory.reservedQuantity}</td>
                <td>
                  <form action={adjustInventoryAction} className="flex gap-2">
                    <input type="hidden" name="productId" value={inventory.productId} />
                    <input className="input w-28" name="delta" type="number" placeholder="+/-" />
                    <button className="btn" type="submit">{t.apply}</button>
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
