/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { AddToCartForm } from "@/components/add-to-cart-form";
import { currentLanguage, safeBackendFetch } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { sampleProducts } from "@/lib/sample-data";
import type { Product } from "@/lib/types";

type ProductPageProps = {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function firstParam(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export default async function ProductPage({ params, searchParams }: ProductPageProps) {
  const { id } = await params;
  const query = await searchParams;
  const cartError = firstParam(query.cartError);
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).product;
  const fallback = {
    ...sampleProducts[0],
    description: "A fallback product shown while backend services are not available locally.",
  };
  const product = await safeBackendFetch<Product>(`/api/products/${id}`, fallback, { language });

  return (
    <div className="page-shell section">
      <div className="split">
        <img className="product-media panel" src={product.imageUrl} alt={product.name} />
        <section className="stack">
          <Link className="chip w-fit" href="/">
            {t.back}
          </Link>
          <div>
            <p className="status">{product.category?.name}</p>
            <h1 className="display mt-4 text-6xl leading-none">{product.name}</h1>
            <p className="hero-copy">{product.description}</p>
          </div>
          <p className="price text-3xl">{product.price} TL</p>
          {cartError ? (
            <div className="notice error">
              <strong>{t.cartError}</strong>
              <span>{cartError}</span>
            </div>
          ) : null}
          <AddToCartForm productId={product.id} redirectTo="/cart" addLabel={t.addToCart} quantityLabel={t.quantity} />
          <div className="panel p-5">
            <h2 className="display text-3xl">{t.reviews}</h2>
            <p className="muted mt-2">{t.reviewsCopy}</p>
          </div>
        </section>
      </div>
    </div>
  );
}
