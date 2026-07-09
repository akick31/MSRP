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
    description: 'A daily sale price guessing game similar to the Price Is Right\'s "One Bid" game but it uses real eBay' +
        ' auction items. Guess the price that 5 of these auction items sold for, the closer you are the higher your score.',
  },
} satisfies Record<string, RouteMeta>;

export const routeList: RouteMeta[] = Object.values(routes);
