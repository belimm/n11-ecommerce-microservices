import { createProductAction, deleteProductAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { sampleCategories, sampleProductPage } from "@/lib/sample-data";
import type { Category, PageResponse, ProductSummary } from "@/lib/types";

export default async function AdminProductsPage() {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin;
  const [categories, productPage] = await Promise.all([
    safeBackendFetch<Category[]>("/api/categories", sampleCategories, { auth: true }),
    safeBackendFetch<PageResponse<ProductSummary>>("/api/products?page=0&size=50", sampleProductPage(0, 50), { auth: true }),
  ]);
  const products = toArrayPage(productPage);

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.products.eyebrow}</p>
        <h1 className="display text-5xl">{t.products.title}</h1>
      </div>
      <section className="panel p-6">
        <h2 className="display text-3xl">{t.products.createTitle}</h2>
        <form action={createProductAction} className="form-grid mt-5">
          <label className="field"><span>{t.products.name}</span><input className="input" name="name" required /></label>
          <label className="field"><span>{t.products.slug}</span><input className="input" name="slug" required /></label>
          <label className="field"><span>{t.products.price}</span><input className="input" name="price" type="number" min="0.01" step="0.01" required /></label>
          <label className="field"><span>{t.products.category}</span><select className="input" name="categorySlug">{categories.map((c) => <option key={c.id} value={c.slug}>{c.name}</option>)}</select></label>
          <label className="field"><span>{t.products.imageUrl}</span><input className="input" name="imageUrl" required /></label>
          <label className="flex items-center gap-2 pt-7"><input name="active" type="checkbox" defaultChecked /> {t.products.active}</label>
          <label className="field md:col-span-2"><span>{t.products.description}</span><textarea className="input min-h-28" name="description" required /></label>
          <button className="btn primary" type="submit">{t.products.create}</button>
        </form>
      </section>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.products.product}</th><th>{t.products.category}</th><th>{t.products.price}</th><th>{t.products.status}</th><th></th></tr></thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>{product.name}<br /><span className="muted">{product.slug}</span></td>
                <td>{product.category?.name}</td>
                <td>{product.price} TL</td>
                <td><span className="status">{product.active ? t.active : t.passive}</span></td>
                <td>
                  <form action={deleteProductAction}>
                    <input type="hidden" name="id" value={product.id} />
                    <button className="btn ghost" type="submit">{t.products.delete}</button>
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
