export const SITE_NAME = 'MSRP';
export const SITE_URL = 'https://msrpgame.com';

export interface RouteMeta {
  path: string;
  title: string;
  description: string;
}

export const routes = {
  home: {
    path: '/',
    title: 'MSRP — Daily eBay Price Guessing Game (Price Is Right Style)',
    description: 'Think you know eBay prices? Guess what 5 real eBay auctions actually sold for in this free daily game, Price Is Right style. New puzzle every day.',
  },
} satisfies Record<string, RouteMeta>;

export const routeList: RouteMeta[] = Object.values(routes);
