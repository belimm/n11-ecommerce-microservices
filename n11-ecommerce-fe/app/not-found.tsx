import Link from "next/link";

export default function NotFound() {
  return (
    <div className="page-shell section">
      <div className="panel p-8">
        <p className="status">404</p>
        <h1 className="display mt-4 text-5xl">This shelf is empty.</h1>
        <Link className="btn primary mt-6" href="/">
          Back to shop
        </Link>
      </div>
    </div>
  );
}
