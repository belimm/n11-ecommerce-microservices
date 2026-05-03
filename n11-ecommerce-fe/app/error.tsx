"use client";

export default function ErrorBoundary({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <div className="page-shell section">
      <div className="panel p-8">
        <p className="status">Error</p>
        <h1 className="display mt-4 text-5xl">Something bent out of shape.</h1>
        <p className="muted mt-3">{error.message}</p>
        <button className="btn primary mt-6" onClick={reset}>
          Try again
        </button>
      </div>
    </div>
  );
}
