export function ebayHighResImageUrl(url: string | undefined | null): string {
  if (!url) return '';
  if (!/ebayimg\.com/i.test(url)) return url;
  return url.replace(/\/s-l\d+/i, '/s-l1600');
}
