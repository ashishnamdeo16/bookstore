import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type {
  DailyOrderItem,
  DailyRevenueItem,
  MonthlyRevenueItem,
  PaymentsAnalyticsResponse,
  TopBookItem,
} from '../../types/analytics';
import { formatPrice } from '../orders/orderFormat';
import { formatMonthLabel, formatShortDate, useChartPalette } from './chartTheme';

function EmptyChart({ message }: { message: string }) {
  return <p className="analytics-empty">{message}</p>;
}

function currencyTick(value: number): string {
  if (value >= 1000) return `$${(value / 1000).toFixed(value % 1000 === 0 ? 0 : 1)}k`;
  return `$${value}`;
}

export function RevenueTrendChart({ data }: { data: DailyRevenueItem[] }) {
  const palette = useChartPalette();
  const chartData = data.map((row) => ({
    ...row,
    label: formatShortDate(row.date),
    revenue: Number(row.revenue),
  }));

  if (chartData.length === 0) {
    return <EmptyChart message="No daily revenue yet." />;
  }

  return (
    <ResponsiveContainer width="100%" height={280}>
      <AreaChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={palette.primary} stopOpacity={0.35} />
            <stop offset="100%" stopColor={palette.primary} stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid stroke={palette.grid} strokeDasharray="3 3" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={{ stroke: palette.grid }}
          tickLine={false}
          minTickGap={28}
        />
        <YAxis
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={false}
          tickLine={false}
          width={48}
          tickFormatter={currencyTick}
        />
        <Tooltip
          contentStyle={{
            background: palette.tooltipBg,
            border: `1px solid ${palette.tooltipBorder}`,
            borderRadius: 10,
            color: palette.text,
          }}
          formatter={(value) => [formatPrice(Number(value ?? 0)), 'Revenue']}
          labelFormatter={(_, payload) => {
            const row = payload?.[0]?.payload as { date?: string } | undefined;
            return row?.date ?? '';
          }}
        />
        <Area
          type="monotone"
          dataKey="revenue"
          stroke={palette.primary}
          strokeWidth={2.5}
          fill="url(#revenueFill)"
          name="Revenue"
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}

export function OrdersTrendChart({ data }: { data: DailyOrderItem[] }) {
  const palette = useChartPalette();
  const chartData = data.map((row) => ({
    ...row,
    label: formatShortDate(row.date),
  }));

  if (chartData.length === 0) {
    return <EmptyChart message="No daily orders yet." />;
  }

  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid stroke={palette.grid} strokeDasharray="3 3" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={{ stroke: palette.grid }}
          tickLine={false}
          minTickGap={28}
        />
        <YAxis
          allowDecimals={false}
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={false}
          tickLine={false}
          width={36}
        />
        <Tooltip
          contentStyle={{
            background: palette.tooltipBg,
            border: `1px solid ${palette.tooltipBorder}`,
            borderRadius: 10,
            color: palette.text,
          }}
          labelFormatter={(_, payload) => {
            const row = payload?.[0]?.payload as { date?: string } | undefined;
            return row?.date ?? '';
          }}
        />
        <Legend wrapperStyle={{ fontSize: 12, color: palette.muted }} />
        <Bar dataKey="ordersCreated" name="Created" fill={palette.tertiary} radius={[4, 4, 0, 0]} />
        <Bar dataKey="paidOrders" name="Paid" fill={palette.secondary} radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}

export function PaymentsDonutChart({ data }: { data: PaymentsAnalyticsResponse }) {
  const palette = useChartPalette();
  const chartData = [
    { name: 'Paid', value: data.paidOrders },
    { name: 'Failed', value: data.failedPayments },
  ].filter((row) => row.value > 0);

  if (chartData.length === 0) {
    return <EmptyChart message="No payment outcomes yet." />;
  }

  const colors = [palette.success, palette.error];

  return (
    <ResponsiveContainer width="100%" height={260}>
      <PieChart>
        <Pie
          data={chartData}
          dataKey="value"
          nameKey="name"
          cx="50%"
          cy="50%"
          innerRadius={58}
          outerRadius={88}
          paddingAngle={2}
          stroke="none"
        >
          {chartData.map((entry, index) => (
            <Cell key={entry.name} fill={colors[index % colors.length]} />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{
            background: palette.tooltipBg,
            border: `1px solid ${palette.tooltipBorder}`,
            borderRadius: 10,
            color: palette.text,
          }}
        />
        <Legend wrapperStyle={{ fontSize: 12, color: palette.muted }} />
      </PieChart>
    </ResponsiveContainer>
  );
}

export function TopBooksChart({ books }: { books: TopBookItem[] }) {
  const palette = useChartPalette();
  const chartData = books.slice(0, 8).map((book) => ({
    name: book.bookTitle.length > 28 ? `${book.bookTitle.slice(0, 26)}…` : book.bookTitle,
    fullName: book.bookTitle,
    quantitySold: book.quantitySold,
    revenue: Number(book.revenue),
  }));

  if (chartData.length === 0) {
    return <EmptyChart message="No paid book sales yet." />;
  }

  return (
    <ResponsiveContainer width="100%" height={Math.max(220, chartData.length * 36)}>
      <BarChart
        data={chartData}
        layout="vertical"
        margin={{ top: 4, right: 16, left: 8, bottom: 4 }}
      >
        <CartesianGrid stroke={palette.grid} strokeDasharray="3 3" horizontal={false} />
        <XAxis
          type="number"
          allowDecimals={false}
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={false}
          tickLine={false}
        />
        <YAxis
          type="category"
          dataKey="name"
          width={140}
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={false}
          tickLine={false}
        />
        <Tooltip
          contentStyle={{
            background: palette.tooltipBg,
            border: `1px solid ${palette.tooltipBorder}`,
            borderRadius: 10,
            color: palette.text,
          }}
          formatter={(value, name) => {
            if (name === 'quantitySold') return [value, 'Qty sold'];
            return [formatPrice(Number(value ?? 0)), 'Revenue'];
          }}
          labelFormatter={(_, payload) => {
            const row = payload?.[0]?.payload as { fullName?: string } | undefined;
            return row?.fullName ?? '';
          }}
        />
        <Bar dataKey="quantitySold" name="Qty sold" fill={palette.primary} radius={[0, 4, 4, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}

export function MonthlyRevenueChart({ data }: { data: MonthlyRevenueItem[] }) {
  const palette = useChartPalette();
  const chartData = data.map((row) => ({
    ...row,
    label: formatMonthLabel(row.month),
    revenue: Number(row.revenue),
  }));

  if (chartData.length === 0) {
    return <EmptyChart message="No monthly revenue yet." />;
  }

  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid stroke={palette.grid} strokeDasharray="3 3" vertical={false} />
        <XAxis
          dataKey="label"
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={{ stroke: palette.grid }}
          tickLine={false}
        />
        <YAxis
          tick={{ fill: palette.muted, fontSize: 12 }}
          axisLine={false}
          tickLine={false}
          width={48}
          tickFormatter={currencyTick}
        />
        <Tooltip
          contentStyle={{
            background: palette.tooltipBg,
            border: `1px solid ${palette.tooltipBorder}`,
            borderRadius: 10,
            color: palette.text,
          }}
          formatter={(value) => [formatPrice(Number(value ?? 0)), 'Revenue']}
        />
        <Bar dataKey="revenue" name="Revenue" fill={palette.secondary} radius={[6, 6, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
