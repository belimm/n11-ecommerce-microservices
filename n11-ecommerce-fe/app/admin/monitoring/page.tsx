import { MonitoringFrame } from "@/components/monitoring-frame";
import { currentLanguage } from "@/lib/backend";
import { dictionary, normalizeLanguage } from "@/lib/i18n";

export default async function AdminMonitoringPage() {
  const grafanaUrl = process.env.NEXT_PUBLIC_GRAFANA_URL ?? "http://localhost:3000";
  const language = normalizeLanguage(await currentLanguage());
  const t = dictionary(language).admin.monitoring;

  return (
    <div className="stack">
      <div>
        <p className="muted">{t.eyebrow}</p>
        <h1 className="display text-5xl">{t.title}</h1>
      </div>
      <MonitoringFrame
        src={grafanaUrl}
        title={t.iframeTitle}
        loadingLabel={t.loading}
        openLabel={t.open}
        hint={t.hint}
      />
    </div>
  );
}
