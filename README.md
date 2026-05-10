# MSRP

A daily game where players guess the final sold price of 5 real eBay auction items. Each item shows the title, image, and number of bids received. Players score 0-100 per item based on how close their guess is relative to the actual price, for a maximum total score of 500.

## Stack

| Layer | Tech |
|---|---|
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Backend | Spring Boot 3.3.5 (Kotlin), MariaDB |

## Game Mechanics

- Five eBay sold listings per day, curated automatically
- Score per item is based on percentage accuracy relative to the actual sale price
- Daily mode tracks win streaks and personal high score
- Previous Day mode lets players replay any past date without affecting daily stats or analytics
- Global stats (high score, low score, average) are shown on the results screen per game date

## Project Structure

```
MSRP/
  backend/    Spring Boot API, eBay item curation
  frontend/   React app
```

### Backend

```
backend/src/main/kotlin/com/msrp/backend/
  controllers/      REST endpoints
  filters/          CORS, admin auth (X-Admin-Key header)
  services/
    GameService         Item fetching, guess verification, available dates
    AnalyticsService    Event rollup, per-date score tracking
    ContactService      Contact form email delivery
    ebay/
      EbayService       eBay sold listing search and curation
  model/            JPA entities and DTOs
  repositories/     Spring Data JPA repositories
  scheduler/        Daily curation job
  util/             Exception handling, DTO conversion, logging
```

### Frontend

```
frontend/src/
  pages/        DailyPage, PastGamePage
  components/   Header, GamePlay, RevealScreen, EndScreen, LoadingScreen,
                HowToPlay, StatsModal, GlobalStatsModal, SettingsModal,
                PastGamePickerModal, ContactModal, LandingPage, Modal
  hooks/        useGameState, useStats, useSettings, useModal
  services/     api.ts
  utils/        share
  types/        index.ts
```

## REST API

Base path: `/api/v1/msrp`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/today` | None | Returns the 5 items for today (or a given date via `?date=`) |
| POST | `/verify` | None | Submit a price guess; returns actual price and score |
| GET | `/available-dates` | None | Dates with available game items |
| GET | `/game-stats` | None | Global high/low/avg score for a given date via `?date=` |
| POST | `/analytics` | None | Record an analytics event |
| POST | `/score` | None | Submit a completed game score for global stat tracking |
| POST | `/contact` | None | Send a contact form email |
| POST | `/admin/curate` | X-Admin-Key | Manually trigger item curation for a date |
| GET | `/admin/analytics` | X-Admin-Key | Analytics event summary |

## Dev Setup

**Backend**

```bash
cd backend
./gradlew bootRun
```

Requires Java 17+. Configuration is in `src/main/resources/application.properties` (not committed). The backend runs on port 777 by default.

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

The dev server runs on port 3000 and proxies `/api` to `localhost:777`.

## Item Curation

A scheduled job runs daily at 9:00 PM UTC and curates eBay sold listings for the next 2 days. Items are pulled from eBay's completed listings search. Curation can also be triggered manually via the `/admin/curate` endpoint.

## Analytics

Three events are tracked per calendar day (EST):

| Event | When |
|---|---|
| `UNIQUE_VISITORS` | Once per browser per day (localStorage-gated) |
| `GAMES_PLAYED` | When a daily game is completed |
| `REPLAY_PLAYED` | When a past game is completed |

Per-date global scores are stored separately in the `game_stats` table with high score, low score, total score, and player count.

## Linting

The backend uses ktlint via the `org.jlleitschuh.gradle.ktlint` Gradle plugin.

```bash
cd backend
./gradlew ktlintCheck   # check for violations
./gradlew ktlintFormat  # auto-fix violations
```
