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
    title: 'MSRP - Daily eBay Price Guessing Game',
    description: 'Guess what 5 real eBay auctions actually sold for in this free daily game in a similar vein to Price is Right. New puzzle every day.',
  },
} satisfies Record<string, RouteMeta>;

export const routeList: RouteMeta[] = Object.values(routes);
