import { useCallback, useEffect, useState } from 'react';
import { analyticsService } from '../../api/analyticsService';
import { AlertBanner } from '../../components/books/AlertBanner';
import { LoadingBlock } from '../../components/books/ConfirmDialog';
import { StatCard } from '../../components/dashboard/StatCard';
import { Button } from '../../components/ui/Button';
import { PageHeader } from '../../components/ui/PageHeader';
import {
  MonthlyRevenueChart,
  OrdersTrendChart,
  PaymentsDonutChart,
  RevenueTrendChart,
  TopBooksChart,
} from '../../features/analytics/AnalyticsCharts';
import { AnalyticsPanel } from '../../features/analytics/AnalyticsPanel';
import { formatPrice } from '../../features/orders/orderFormat';
import { ApiError } from '../../types/api';
import type {
  BooksAnalyticsResponse,
  DashboardResponse,
  OrdersAnalyticsResponse,
  PaymentsAnalyticsResponse,
  RevenueResponse,
} from '../../types/analytics';

function formatRate(value: number): string {
  return `${Number(value).toFixed(1)}%`;
}

export function AdminAnalyticsPage() {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [revenue, setRevenue] = useState<RevenueResponse | null>(null);
  const [orders, setOrders] = useState<OrdersAnalyticsResponse | null>(null);
  const [books, setBooks] = useState<BooksAnalyticsResponse | null>(null);
  const [payments, setPayments] = useState<PaymentsAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [dash, rev, ord, bookData, pay] = await Promise.all([
        analyticsService.getDashboard(),
        analyticsService.getRevenue(),
        analyticsService.getOrders(),
        analyticsService.getBooks(),
        analyticsService.getPayments(),
      ]);
      setDashboard(dash);
      setRevenue(rev);
      setOrders(ord);
      setBooks(bookData);
      setPayments(pay);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : 'Could not load analytics. Please try again.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) {
    return <LoadingBlock label="Loading analytics" />;
  }

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Admin Portal"
        title="Analytics"
        subtitle="Live metrics from the analytics service — orders, revenue, payments, and bestsellers."
        actions={
          <Button variant="secondary" size="sm" onClick={() => void load()}>
            Refresh
          </Button>
        }
      />

      {error ? (
        <AlertBanner variant="error" title="Failed to load analytics">
          {error}
        </AlertBanner>
      ) : null}

      {dashboard ? (
        <div className="stat-grid">
          <StatCard label="Total orders" value={dashboard.totalOrders} hint="Order created" />
          <StatCard label="Paid orders" value={dashboard.paidOrders} hint="Payment success" />
          <StatCard
            label="Failed payments"
            value={dashboard.failedPayments}
            hint="Payment failed"
          />
          <StatCard
            label="Total revenue"
            value={formatPrice(Number(dashboard.totalRevenue))}
            hint="From paid orders"
          />
          <StatCard
            label="Avg order value"
            value={formatPrice(Number(dashboard.averageOrderValue))}
            hint="Revenue / paid orders"
          />
          <StatCard label="Books sold" value={dashboard.booksSold} hint="Units paid" />
          <StatCard
            label="Payment success rate"
            value={formatRate(dashboard.paymentSuccessRate)}
            hint="Paid / (paid + failed)"
          />
        </div>
      ) : null}

      <div className="analytics-grid">
        {revenue ? (
          <AnalyticsPanel
            className="analytics-panel--wide"
            title="Revenue trend"
            subtitle="Daily revenue over the last 30 days"
          >
            <RevenueTrendChart data={revenue.daily} />
          </AnalyticsPanel>
        ) : null}

        {orders ? (
          <AnalyticsPanel
            className="analytics-panel--wide"
            title="Orders trend"
            subtitle="Created vs paid orders by day"
          >
            <OrdersTrendChart data={orders.daily} />
          </AnalyticsPanel>
        ) : null}

        {payments ? (
          <AnalyticsPanel
            title="Payment outcomes"
            subtitle={`${formatRate(payments.paymentSuccessRate)} success rate`}
          >
            <PaymentsDonutChart data={payments} />
            <dl className="analytics-kpi-row">
              <div>
                <dt>Paid</dt>
                <dd>{payments.paidOrders}</dd>
              </div>
              <div>
                <dt>Failed</dt>
                <dd>{payments.failedPayments}</dd>
              </div>
            </dl>
          </AnalyticsPanel>
        ) : null}

        {books ? (
          <AnalyticsPanel
            title="Top selling books"
            subtitle={`${books.booksSold} units sold overall`}
          >
            <TopBooksChart books={books.topBooks} />
          </AnalyticsPanel>
        ) : null}

        {revenue && revenue.monthly.length > 0 ? (
          <AnalyticsPanel
            className="analytics-panel--wide"
            title="Monthly revenue"
            subtitle="Aggregated paid revenue by month"
          >
            <MonthlyRevenueChart data={revenue.monthly} />
          </AnalyticsPanel>
        ) : null}

        {books && books.topBooks.length > 0 ? (
          <AnalyticsPanel
            className="analytics-panel--wide"
            title="Bestsellers detail"
            subtitle="Quantity and revenue by title"
          >
            <div className="md-table-wrap">
              <table className="md-table">
                <thead>
                  <tr>
                    <th>Book</th>
                    <th>Qty sold</th>
                    <th>Revenue</th>
                  </tr>
                </thead>
                <tbody>
                  {books.topBooks.map((book) => (
                    <tr key={book.bookId}>
                      <td>{book.bookTitle}</td>
                      <td>{book.quantitySold}</td>
                      <td>{formatPrice(Number(book.revenue))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </AnalyticsPanel>
        ) : null}
      </div>
    </div>
  );
}
