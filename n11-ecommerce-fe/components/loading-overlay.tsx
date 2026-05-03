type LoadingOverlayProps = {
  label?: string;
  fullscreen?: boolean;
};

export function LoadingOverlay({ label = "Loading", fullscreen = true }: LoadingOverlayProps) {
  return (
    <div className={fullscreen ? "loading-screen" : "loading-panel"} role="status" aria-live="polite" aria-label={label}>
      <div className="loading-card">
        <span className="loading-orb" aria-hidden="true"><span /></span>
        <span>{label}</span>
      </div>
    </div>
  );
}
