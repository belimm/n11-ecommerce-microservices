import { createCategoryAction, deleteCategoryAction } from "@/app/actions";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { sampleCategories } from "@/lib/sample-data";
import type { Category } from "@/lib/types";

export default async function AdminCategoriesPage() {
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin.categories;
  const categories = await safeBackendFetch<Category[]>("/api/categories", sampleCategories, { auth: true });

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <section className="panel p-6">
        <h2 className="display text-3xl">{t.createTitle}</h2>
        <form action={createCategoryAction} className="form-grid mt-5">
          <label className="field"><span>{t.name}</span><input className="input" name="name" required /></label>
          <label className="field"><span>{t.slug}</span><input className="input" name="slug" required /></label>
          <label className="field md:col-span-2"><span>{t.description}</span><textarea className="input min-h-24" name="description" /></label>
          <button className="btn primary" type="submit">{t.create}</button>
        </form>
      </section>
      <div className="table-wrap">
        <table>
          <thead><tr><th>{t.name}</th><th>{t.slug}</th><th>{t.description}</th><th></th></tr></thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id}>
                <td>{category.name}</td>
                <td>{category.slug}</td>
                <td>{category.description}</td>
                <td>
                  <form action={deleteCategoryAction}>
                    <input type="hidden" name="id" value={category.id} />
                    <button className="btn ghost" type="submit">{t.delete}</button>
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
