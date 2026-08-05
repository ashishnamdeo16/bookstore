export function AppFooter() {
  const year = new Date().getFullYear();

  return (
    <footer className="portal-footer">
      <div className="portal-footer__inner">
        <p className="portal-footer__copy">© {year} Bookstore</p>
        <p className="portal-footer__tagline">Curated reads, delivered with care.</p>
      </div>
    </footer>
  );
}
