export interface DailyItem {
  id: number;
  ebay_item_id: string;
  game_date: string;
  title: string;
  image_url: string;
  bid_count: number;
  item_url: string;
  price_choices: number[];
  sale_date?: string;
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

export type ModalId = 'how-to-play' | 'stats' | 'global-stats' | 'settings' | 'past-picker' | 'contact' | 'projects';

export interface GlobalStats {
  date: string;
  highScore: number | null;
  lowScore: number | null;
  avgScore: number | null;
  playerCount: number;
}

export interface PlayerStats {
  currentStreak: number;
  maxStreak: number;
  gamesPlayed: number;
  lastPlayedDate: string;
  highScore: number;
  lowScore: number | null;
  totalScore: number;
  scoreCount: number;
}

export interface GameProgress {
  date: string;
  currentRound: number;
  results: RoundResult[];
}

export type GameState = 'loading' | 'landing' | 'playing' | 'revealing' | 'finished';
