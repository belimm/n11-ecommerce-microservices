"use client";

import { useUiStore } from "@/stores/ui-store";

export function CartBadge() {
  const count = useUiStore((state) => state.optimisticCartCount);
  if (count === 0) return null;

  return (
    <span className="status" aria-label={`${count} optimistic cart additions`}>
      +{count}
    </span>
  );
}
