# MSRP

A daily price guessing game. Five real eBay auction items are shown each day with their title, image, and bid count. Players choose from four price options per item and score 0-100 based on how close their guess is to the actual sold price, for a maximum daily total of 500.

## Stack

| Layer | Tech |
|---|---|
| Frontend | React + TypeScript + Vite, Tailwind CSS (custom `msrp-*` color tokens) |
| Backend | Spring Boot 3.3.5 (Kotlin), MariaDB, Jsoup (eBay scraping) |
| Build | Gradle (Kotlin DSL) for backend, npm/Vite for frontend |

The frontend proxies `/api` to the backend in dev. In production the backend serves on port 777; the frontend is a separate static build.

## REST API (`/api/v1/msrp`)

| Method | Path | Notes |
|---|---|---|
| GET | `/today?date=YYYY-MM-DD` | Returns today's (or specified date's) 5 items with price choices |
| POST | `/verify` | Body: `{ itemId, guess }` -- returns score and actual price |
| GET | `/available-dates` | List of past dates with games, for the replay picker |
| POST | `/analytics` | Body: `{ eventType }` -- records UNIQUE_VISITORS, GAMES_PLAYED, REPLAY_PLAYED |
| POST | `/score` | Body: `{ score, date }` -- submits final score to global leaderboard |
| GET | `/game-stats?date=YYYY-MM-DD` | Returns high/low/avg score for a game date |
| POST | `/contact` | Body: `{ name, email, subject, message }` -- sends email |
| POST | `/admin/curate` | Auth-gated; triggers eBay item curation for a date |
| GET | `/admin/analytics?days=30` | Auth-gated; returns event + game stats summary |

## Game mechanics

- 5 eBay sold auction listings per day, curated automatically each night at 9 PM
- Each round shows the item title, image, bid count, and 4 price choices (one correct, three decoys)
- Score per item: 100 points for exact match, scaling down based on percentage off
- Maximum total score per day: 500
- Decoy prices are generated as 25%, 50%, and 250% of the actual price, rounded to natural increments
- Past Game mode replays any prior date without affecting streaks or analytics

## Score calculation

Scoring is percentage-based relative to the actual price. A guess within a small margin earns close to 100; a guess far off earns close to 0. The formula is applied per item.

## Item curation

- Runs nightly at 9 PM via `@Scheduled` in `EbayCuratorScheduler`
- Curates items for the next 1-2 days
- Scrapes eBay sold auction listings by category using Jsoup
- Filters for items with images, valid prices, and reasonable bid counts
- Stores items in the `daily_item` table keyed by `game_date`

## Admin auth

The `/admin/*` endpoints require an `X-Admin-Key` header matching `msrp.admin.api-key` in `application.properties`. The check uses constant-time comparison to prevent timing attacks.

## Dev setup

```bash
# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev
```

Frontend dev server proxies `/api` to `localhost:777`.

## Config notes (`application.properties`)

- `spring.mail.password` must be a Gmail App Password (not the account password). Generate at myaccount.google.com -- Security -- App passwords.
- `msrp.admin.api-key` gates the `/admin/*` endpoints.
- `cors.allowed.origins` is a comma-separated list of allowed frontend origins.
