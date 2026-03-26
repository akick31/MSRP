export interface DailyItem {
  id: number;
  ebay_item_id: string;
  title: string;
  image_url: string;
  bid_count: number;
}

export interface VerifyRequest {
  item_id: number;
  guess: number;
}

export interface VerifyResponse {
  item_id: number;
  guess: number;
  actual_price: number;
  percentage_off: number;
  score: number;
}

export interface RoundResult {
  item: DailyItem;
  guess: number;
  actualPrice: number;
  percentageOff: number;
  score: number;
}

export interface PlayerStats {
  currentStreak: number;
  maxStreak: number;
  gamesPlayed: number;
  lastPlayedDate: string;
}

export interface GameProgress {
  date: string;
  currentRound: number;
  results: RoundResult[];
}

export type GameState = 'loading' | 'playing' | 'revealing' | 'finished';
