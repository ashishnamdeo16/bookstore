import { useMemo } from 'react';
import { useTheme } from '../../theme/ThemeProvider';

export interface ChartPalette {
  primary: string;
  secondary: string;
  tertiary: string;
  success: string;
  error: string;
  muted: string;
  grid: string;
  tooltipBg: string;
  tooltipBorder: string;
  text: string;
}

function readCssVar(name: string, fallback: string): string {
  if (typeof window === 'undefined') return fallback;
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  return value || fallback;
}

export function useChartPalette(): ChartPalette {
  const { resolvedTheme } = useTheme();

  return useMemo(() => {
    const dark = resolvedTheme === 'dark';
    return {
      primary: readCssVar('--md-primary', dark ? '#a898d8' : '#5b4b8a'),
      secondary: dark ? '#6a9b8e' : '#3d6b5c',
      tertiary: dark ? '#c4a574' : '#8b6b4a',
      success: readCssVar('--md-success', dark ? '#a5d6a7' : '#2e7d32'),
      error: readCssVar('--md-error', dark ? '#ef9a9a' : '#c62828'),
      muted: readCssVar('--md-on-surface-variant', dark ? '#b8aea2' : '#6b635a'),
      grid: readCssVar('--md-outline', dark ? '#3d362e' : '#e4ddd3'),
      tooltipBg: readCssVar('--md-surface-elevated', dark ? '#2f2a23' : '#ffffff'),
      tooltipBorder: readCssVar('--md-outline', dark ? '#3d362e' : '#e4ddd3'),
      text: readCssVar('--md-on-surface', dark ? '#f5f0e8' : '#1a1612'),
    };
  }, [resolvedTheme]);
}

export function formatShortDate(value: string): string {
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric' }).format(date);
}

export function formatMonthLabel(value: string): string {
  // Expect YYYY-MM
  const [year, month] = value.split('-');
  if (!year || !month) return value;
  const date = new Date(Number(year), Number(month) - 1, 1);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: '2-digit' }).format(date);
}
