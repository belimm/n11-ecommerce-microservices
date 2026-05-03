import Link from "next/link";
import { currentLanguage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";
import { requireAdmin } from "@/lib/session";

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  await requireAdmin();
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin;

  const adminLinks = [
    ["/admin/products", t.nav.products],
    ["/admin/categories", t.nav.categories],
    ["/admin/orders", t.nav.orders],
    ["/admin/customers", t.nav.customers],
    ["/admin/inventory", t.nav.inventory],
    ["/admin/payments", t.nav.payments],
    ["/admin/monitoring", t.nav.monitoring],
  ];

  return (
    <div className="page-shell admin-layout">
      <aside>
        <p className="status">Admin</p>
        <nav className="admin-nav mt-4" aria-label="Admin navigation">
          {adminLinks.map(([href, label]) => (
            <Link href={href} key={href}>
              {label}
            </Link>
          ))}
        </nav>
      </aside>
      <section>{children}</section>
    </div>
  );
}
