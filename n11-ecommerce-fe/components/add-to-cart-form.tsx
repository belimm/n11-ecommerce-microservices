"use client";

import { addCartItemAction } from "@/app/actions";
import { useUiStore } from "@/stores/ui-store";

export function AddToCartForm({
  productId,
  redirectTo = "/cart",
  compact = false,
  addLabel = "Add to cart",
  quantityLabel = "Quantity",
}: {
  productId: number;
  redirectTo?: string;
  compact?: boolean;
  addLabel?: string;
  quantityLabel?: string;
}) {
  const bumpCart = useUiStore((state) => state.bumpCart);

  return (
    <form action={addCartItemAction} className={compact ? "flex gap-2" : "flex flex-wrap gap-3"}>
      <input type="hidden" name="productId" value={productId} />
      <input type="hidden" name="redirectTo" value={redirectTo} />
      <input
        className={compact ? "input w-20" : "input max-w-28"}
        name="quantity"
        type="number"
        min="1"
        defaultValue="1"
        aria-label={quantityLabel}
      />
      <button className="btn primary" type="submit" onClick={() => bumpCart()}>
        {addLabel}
      </button>
    </form>
  );
}
