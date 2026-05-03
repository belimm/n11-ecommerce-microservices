"use client";

import { useState } from "react";

type MonitoringFrameProps = {
  src: string;
  title: string;
  loadingLabel: string;
  openLabel: string;
  hint: string;
};

export function MonitoringFrame({ src, title, loadingLabel, openLabel, hint }: MonitoringFrameProps) {
  const [loaded, setLoaded] = useState(false);

  return (
    <section className="monitoring-shell">
      {!loaded ? (
        <div className="monitoring-loading">
          <span className="loading-orb" aria-hidden="true"><span /></span>
          <span>{loadingLabel}</span>
        </div>
      ) : null}
      <iframe
        className="monitoring-frame"
        src={src}
        title={title}
        onLoad={() => setLoaded(true)}
      />
      <div className="monitoring-footnote">
        <span>{hint}</span>
        <a className="btn ghost" href={src} target="_blank" rel="noreferrer">{openLabel}</a>
      </div>
    </section>
  );
}
