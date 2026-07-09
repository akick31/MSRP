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
    description: 'A daily sale price guessing game similar to The Price Is Right\'s "One Bid". The game takes real ' +
        'eBay auction items and your goal is to guess the final sale price of five items. The closer your estimate, ' +
        'the higher your score.'
  },
} satisfies Record<string, RouteMeta>;

export const routeList: RouteMeta[] = Object.values(routes);
