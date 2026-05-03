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
      </body>
    </html>
  );
}
