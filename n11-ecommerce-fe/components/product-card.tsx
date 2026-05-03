/* eslint-disable @next/next/no-img-element */
import Link from "next/link";
import { AddToCartForm } from "@/components/add-to-cart-form";
import type { ProductSummary } from "@/lib/types";

export function ProductCard({
  product,
  detailsLabel = "Details",
  addLabel = "Add to cart",
  quantityLabel = "Quantity",
}: {
  product: ProductSummary;
  detailsLabel?: string;
  addLabel?: string;
  quantityLabel?: string;
}) {
  return (
    <article className="product-card">
      <Link href={`/products/${product.id}`} aria-label={product.name}>
        <img className="product-media" src={product.imageUrl} alt={product.name} />
      </Link>
      <div className="product-body">
        <div>
          <p className="muted text-sm">{product.category?.name}</p>
          <h3 className="display text-2xl leading-none">{product.name}</h3>
        </div>
        <div className="flex items-center justify-between gap-3">
          <span className="price">{product.price} TL</span>
          <Link className="chip" href={`/products/${product.id}`}>
            {detailsLabel}
          </Link>
        </div>
        <AddToCartForm productId={product.id} compact addLabel={addLabel} quantityLabel={quantityLabel} />
      </div>
    </article>
  );
}
