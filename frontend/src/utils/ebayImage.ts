/**
 * eBay image CDN paths include `/s-lNNN` (thumbnail width). The same image id is available at gallery size.
 * Applies to URLs scraped from SERP and fixes already-persisted rows in the DB.
 */
export function ebayHighResImageUrl(url: string | undefined | null): string {
  if (!url) return '';
  if (!/ebayimg\.com/i.test(url)) return url;
  return url.replace(/\/s-l\d+/i, '/s-l1600');
}
