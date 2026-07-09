import { API_BASE_URL } from '../services/api';

const API_ORIGIN = new URL(API_BASE_URL, window.location.origin).origin;

export function ebayHighResImageUrl(url: string | undefined | null): string {
  if (!url) return '';
  if (url.startsWith('/')) return `${API_ORIGIN}${url}`;
  if (!/ebayimg\.com/i.test(url)) return url;
  return url.replace(/\/s-l\d+/i, '/s-l1600');
}
