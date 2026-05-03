import Link from "next/link";
import { ProductCard } from "@/components/product-card";
import { currentLanguage, safeBackendFetch, toArrayPage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { sampleCategories, sampleProductPage } from "@/lib/sample-data";
import { getSession } from "@/lib/session";
import type { Category, PageResponse, ProductSummary } from "@/lib/types";

type HomeProps = {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export default async function Home({ searchParams }: HomeProps) {
  const params = await searchParams;
  const page = Number(firstParam(params.page) ?? "0");
  const categorySlug = firstParam(params.categorySlug);
  const cartError = firstParam(params.cartError);
  const language = normalizeLanguage(firstParam(params.lang) ?? (await currentLanguage()));
  const session = await getSession();
  const showCategoryBrowse = Boolean(session && session.role !== "ADMIN");
  const t = dictionary(language).home;
  const query = new URLSearchParams({
    page: String(Number.isFinite(page) ? page : 0),
    size: "12",
  });
  if (categorySlug) query.set("categorySlug", categorySlug);

  const [categories, productPage] = await Promise.all([
    showCategoryBrowse ? safeBackendFetch<Category[]>("/api/categories", sampleCategories, { language }) : Promise.resolve([] as Category[]),
    safeBackendFetch<PageResponse<ProductSummary>>(`/api/products?${query}`, sampleProductPage(page), { language }),
  ]);
  const products = toArrayPage(productPage);

  return (
    <div className="page-shell">
      <section className="hero-band">
        <div>
          <p className="status">{t.status}</p>
          <h1 className="display hero-title">{t.title}</h1>
          <p className="hero-copy">
            {t.copy}
          </p>
        </div>
        {showCategoryBrowse ? (
          <div className="panel p-6">
            <p className="muted">{t.browse}</p>
            <div className="mt-4 flex flex-wrap gap-2">
              <Link className="chip" href="/">
                {t.all}
              </Link>
              {categories.map((category) => (
                <Link className="chip" key={category.id} href={`/?categorySlug=${category.slug}`}>
                  {category.name}
                </Link>
              ))}
            </div>
          </div>
        ) : null}
      </section>

      <section className="section">
        <div className="toolbar">
          <h2 className="display text-4xl">{t.products}</h2>
          <span className="muted">{productPage.totalElements} {t.items}</span>
        </div>
        {cartError ? (
          <div className="notice error">
            <strong>{t.cartError}</strong>
            <span>{cartError}</span>
          </div>
        ) : null}
        <div className="grid-products">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} detailsLabel={dictionary(language).product.details} addLabel={dictionary(language).product.addToCart} quantityLabel={dictionary(language).product.quantity} />
          ))}
        </div>
      </section>
    </div>
  );
}
