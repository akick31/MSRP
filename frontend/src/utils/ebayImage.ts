import { API_BASE_URL } from '../services/api';

export function ebayHighResImageUrl(url: string | undefined | null): string {
  if (!url) return '';
  if (url.startsWith('/')) {
    if (typeof window === 'undefined') return url;
    const origin = new URL(API_BASE_URL, window.location.origin).origin;
    return `${origin}${url}`;
  }
  if (!/ebayimg\.com/i.test(url)) return url;
  return url.replace(/\/s-l\d+/i, '/s-l1600');
}
