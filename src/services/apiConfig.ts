import {Capacitor} from '@capacitor/core';

const SERVER_URL_KEY = 'rmz_cloud_api_base_v1';
const DEFAULT_NATIVE_API_BASE = 'https://ais-pre-5dqrp6vmbjkluesjlbth6l-737153117526.europe-west2.run.app';

export function getCloudApiBaseUrl(): string {
  if (!Capacitor.isNativePlatform()) {
    return '';
  }
  try {
    const saved = localStorage.getItem(SERVER_URL_KEY)?.trim();
    if (saved) return saved.replace(/\/+$/, '');
  } catch {
    // ignore storage errors
  }
  return DEFAULT_NATIVE_API_BASE;
}

export function setCloudApiBaseUrl(url: string): void {
  try {
    const cleaned = url.trim().replace(/\/+$/, '');
    if (cleaned) {
      localStorage.setItem(SERVER_URL_KEY, cleaned);
    } else {
      localStorage.removeItem(SERVER_URL_KEY);
    }
  } catch {
    // ignore
  }
}

export function apiUrl(path: string): string {
  const base = getCloudApiBaseUrl();
  if (!base) return path;
  return `${base}${path.startsWith('/') ? path : `/${path}`}`;
}
