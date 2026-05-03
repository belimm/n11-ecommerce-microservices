import type { Category, PageResponse, ProductSummary } from "@/lib/types";

export const sampleCategories: Category[] = [
  { id: 1, name: "Electronics", slug: "electronics", description: "Phones, computers and smart devices" },
  { id: 2, name: "Fashion", slug: "fashion", description: "Apparel, shoes and accessories" },
  { id: 3, name: "Home & Living", slug: "home-living", description: "Appliances, kitchen and decoration" },
  { id: 4, name: "Sports & Outdoor", slug: "sports-outdoor", description: "Training and outdoor gear" },
];

export const sampleProducts: ProductSummary[] = [
  {
    id: 1,
    name: "Samsung Galaxy S24 Ultra 256 GB",
    slug: "galaxy-s24-ultra-256gb",
    price: "64999.00",
    imageUrl: "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=900&q=80",
    active: true,
    locale: "en",
    category: sampleCategories[0],
  },
  {
    id: 2,
    name: "Air Knit Running Sneaker",
    slug: "running-sneaker-air-knit",
    price: "2199.90",
    imageUrl: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
    active: true,
    locale: "en",
    category: sampleCategories[1],
  },
  {
    id: 3,
    name: "Philips Airfryer XL",
    slug: "philips-airfryer-xl",
    price: "4999.90",
    imageUrl: "https://images.unsplash.com/photo-1626200419199-391ae4be7a41?auto=format&fit=crop&w=900&q=80",
    active: true,
    locale: "en",
    category: sampleCategories[2],
  },
];

export function sampleProductPage(page = 0, size = 12): PageResponse<ProductSummary> {
  return {
    items: sampleProducts,
    page,
    size,
    totalElements: sampleProducts.length,
    totalPages: 1,
    last: true,
  };
}
