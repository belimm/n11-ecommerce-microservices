"use client";

import { usePathname, useSearchParams } from "next/navigation";
import { setLanguageAction } from "@/app/actions";
import type { Language } from "@/lib/i18n";

export function LanguageSwitcher({ language }: { language: Language }) {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const redirectTo = `${pathname}${searchParams.toString() ? `?${searchParams}` : ""}`;

  return (
    <>
      <form action={setLanguageAction}>
        <input type="hidden" name="language" value="tr" />
        <input type="hidden" name="redirectTo" value={redirectTo} />
        <button className={language === "tr" ? "active" : undefined} type="submit" title="Turkce">
          TR
        </button>
      </form>
      <form action={setLanguageAction}>
        <input type="hidden" name="language" value="en" />
        <input type="hidden" name="redirectTo" value={redirectTo} />
        <button className={language === "en" ? "active" : undefined} type="submit" title="English">
          EN
        </button>
      </form>
    </>
  );
}
