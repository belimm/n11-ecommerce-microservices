import type { Metadata } from "next";
import "./globals.css";
import { StoreHeader } from "@/components/store-header";
import { currentLanguage } from "@/lib/backend";
import { normalizeLanguage } from "@/lib/i18n";
import { getSession } from "@/lib/session";

export const metadata: Metadata = {
  title: "n11 Market",
  description: "Broad marketplace storefront and admin operations for the N11 bootcamp ecommerce platform.",
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const session = await getSession();
  const language = normalizeLanguage(await currentLanguage());

  return (
    <html lang={language} className="h-full">
      <body>
        <StoreHeader session={session} />
        <main className="min-h-[calc(100vh-88px)]">{children}</main>
        <footer className="site-footer">
          <div className="page-shell">
            <p>
              Bu proje{" "}
              <a href="https://www.belim.dev" target="_blank" rel="noopener noreferrer" className="footer-link">
                Berk Limoncu
              </a>{" "}
              tarafından n11 Talenthub Backend Bootcamp&apos;i için geliştirilmiştir.
            </p>
          </div>
        </footer>
      </body>
    </html>
  );
}
