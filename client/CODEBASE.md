# Property Intelligence Platform — Frontend Client Codebase Guide

This document provides a comprehensive walkthrough of the Property Intelligence Platform frontend client (`/client`). It is designed for engineers and developers to understand the project architecture, state management, design systems, visual configurations, API integration layers, and components down to the implementation details.

---

## 1. Product Positioning & Constraints

Unlike general real estate listing portals or client-facing consumer apps, this platform functions as a **financial data terminal** for the Nigerian residential property market. The design and UX strictly adhere to several core principles:

- **No-Photograph Constraint**: Instead of photographs, layout structures rely entirely on rich, high-density data visualizations (trend charts, sparklines, price percentile bars) and clear status indicators (such as Days-on-Market traffic lights).
- **Lagos-Aware Data Density**: Typography, symbols, and groupings highlight Nigerian currency treatments (₦) and neighbourhood categorizations (Lekki Phase 1, Ajah, Victoria Island, Ikeja GRA, etc.).
- **Typography Pairing**: Pairs **Inter** (for UI structures and text) with **DM Mono** (for numerical statistics, dates, and prices). Monospace rendering ensures table columns align vertically and elements do not shift during updates.
- **Vanilla CSS styling**: The project utilizes custom HSL CSS properties inside a global stylesheet without relying on Tailwind CSS or utility layout frameworks.

---

## 2. Directory Layout & Architecture

The client directory is laid out as follows:

```text
client/
├── .env                    # Local environment config (VITE_API_BASE_URL)
├── .env.example            # Environment variables scaffold
├── package.json            # Tooling and scripts definitions
├── vite.config.ts          # Vite bundler and development proxy configurations
├── tsconfig.json           # Compiler targets and strict rules configuration
├── src/
│   ├── main.tsx            # Application mounting and bootstrap point
│   ├── App.tsx             # Root routing, query client initialization, silent refresh
│   ├── api/                # API client configuration and resource endpoints
│   │   ├── client.ts       # Axios instance with request/response interceptors
│   │   ├── auth.ts         # User logins, registrations, and refresh actions
│   │   ├── listings.ts     # Property listing queries, details, and nearby endpoints
│   │   ├── market.ts       # Neighbourhood metrics, aggregates, and listing flows
│   │   └── alerts.ts       # Saved query alerts and email subscription center
│   ├── store/              # Zustand memory credential stores
│   │   └── authStore.ts    # In-memory JWT access token store (anti-XSS vector)
│   ├── hooks/              # Custom React hooks
│   │   └── useListingFilters.ts # Synchronizes state filters with URL query parameters
│   ├── styles/             # Stylesheet configuration
│   │   └── globals.css     # Global custom CSS properties, resets, utility classes, and transitions
│   ├── types/              # Type definitions matching backend data contracts
│   │   └── api.d.ts        # Data structures for listings, metrics, and profiles
│   ├── utils/              # Utility helpers
│   │   └── format.ts       # Price formatting and Naira-Kobo conversions
│   ├── pages/              # Routed screen view compositions
│   │   ├── Home.tsx        # Search, market snapshots, trending areas, recent listings
│   │   ├── Listings.tsx    # Split listings grid and Leaflet map, filters panel
│   │   ├── ListingDetail.tsx # Listing metadata view, price histories, minimap preview
│   │   ├── Market.tsx      # Aggregate markets dashboard, summary metrics
│   │   ├── Neighbourhood.tsx # Neighbourhood pricing trend dashboard, percentile graphs
│   │   ├── Alerts.tsx      # User's active subscription criteria manager
│   │   ├── Login.tsx       # Auth card for logging in
│   │   ├── Register.tsx    # Auth card for registering
│   │   └── NotFound.tsx    # Responsive 404 handler with fallback action redirection
│   └── components/         # Reusable presentation and interaction components
│       ├── auth/           # Login wrappers and Protected Route gatekeepers
│       ├── layout/         # Shell headers, footers, drawers, and filter bars
│       ├── listing/        # Map clustering views, sparklines, listing cards, timelines
│       ├── market/         # Statistics tiles, price percentile curves, trend layouts
│       ├── primitives/     # Core inputs, buttons, select boxes, skeletons, badges
│       └── toast/          # Contextual notification manager overlays
```

---

## 3. Styling & Theming (`src/styles/globals.css`)

The visual design system is defined in [globals.css](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/styles/globals.css). It implements a flat-first, low-glare dark palette mimicking financial terminals.

### Core Variables & Accent Tokens
- **Backgrounds**: `--color-bg-base` (`#0C0F14`, deep ink), `--color-bg-raised` (`#13171F`, cards), `--color-bg-overlay` (`#1A202C`, modals).
- **Accent**: Lagos Amber (`var(--color-amber-400)` / `#F5B942`) is reserved for primary CTAs, active states, active navigation links, and focus outlines.
- **Market Signals**: Positive trends use emerald green (`--color-signal-up`), price drops and stale items use rose red (`--color-signal-down`).
- **Focus Indicator**: Standardized glow outline (`box-shadow: 0 0 0 3px var(--color-focus)`) applied to elements on keyboard tab navigation.

### Key Layout and Spacing System
Spacing is built on a base-4 grid system (from `--space-1` = 4px up to `--space-24` = 96px). Standard card paddings default to `--space-6` (24px) with inner gaps set to `--space-4` (16px).

### Utility Classes
To ensure design cohesion without utility frameworks, the bottom of [globals.css](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/styles/globals.css) houses a set of semantic utility classes:
- **Layouts & Flex**: `.flex`, `.flex-wrap`, `.justify-between`, `.items-center`, `.gap-1` to `.gap-4`, `.flex-1`.
- **Grids**: `.grid`, `.grid-cols-1` to `.grid-cols-3` layouts.
- **Styling**: `.bg-raised`, `.bg-subtle`, `.border`, `.border-default`, `.border-strong`, `.rounded-lg`, `.rounded-full`.
- **Text & Colors**: `.text-primary`, `.text-secondary`, `.text-tertiary`, `.text-amber`, `.font-bold`, `.font-semibold`, `.uppercase`, `.tracking-tight`, `.text-center`.

---

## 4. State Management Layer

State management is separated into server caching (React Query) and client credentials (Zustand).

### 4.1 In-Memory Zustand Store (`src/store/authStore.ts`)
To prevent XSS (Cross-Site Scripting) access token hijacking, the client maintains credential state strictly in-memory. Tokens are never persisted to `localStorage` or browser cookie storage.
- **State Properties**: `accessToken` (string | null), `expiresIn` (number | null).
- **Mutators**: `setToken(token, expires)` stores the access token; `clearToken()` purges authorization states.

### 4.2 React Query & API Cache Caching
Data updates, paginated listings, and search results are query-managed via `@tanstack/react-query` with a declarative, key-bound cache structure:
- Recent Listings key: `['listings', 'recent']`
- Filtered Listings key: `['listings', filters]`
- Neighbourhood Stats key: `['market', 'stats', name]`
- Alert Configurations key: `['alerts']`

---

## 5. API Integration Layer (`src/api/`)

The integration layer wraps Axios calls to match the Java Spring Boot REST API endpoints.

### 5.1 Main Axios Instance (`src/api/client.ts`)
- **Initialization**: Automatically fetches `VITE_API_BASE_URL` from the environment. If it points to `/`, Axios targets same-origin endpoints (enabling the Vite dev server proxy).
- **Request Interceptor**: Inspects the Zustand store. If a valid `accessToken` is present, it injects it into the outbound request's headers:
  ```typescript
  config.headers.Authorization = `Bearer ${token}`;
  ```
- **Response Interceptor (Silent Refresh)**: Intercepts `401 Unauthorized` responses. If an API request fails with a 401:
  1. It flags the request as a retry attempt (`_retry = true`).
  2. It fires a silent token refresh POST query to `/auth/refresh` (relies on HttpOnly refresh cookies set by the backend).
  3. If successful, it updates the in-memory Zustand store and retries the original failed request with the new access token.
  4. If the refresh fails (i.e. refresh token expired), it clears the token and redirects the browser to `/login` while appending the `redirect` route parameter.

### 5.2 API Handlers
- **[auth.ts](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/api/auth.ts)**: Interacts with user auth routes (`/auth/login`, `/auth/register`, `/auth/refresh`, `/auth/logout`).
- **[listings.ts](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/api/listings.ts)**: Includes `search()` for paginated listing searches and `getById()` to pull detail specs.
- **[market.ts](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/api/market.ts)**: Exposes `getNeighbourhoods()` (trending listing charts, volume aggregates) and `getStats(neighbourhood)` (WoW index charts, price drop metrics, average lifetime ranges).
- **[alerts.ts](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/api/alerts.ts)**: Integrates saved parameters management (`/alerts`), supporting alert creation and deletion.

---

## 6. Page-by-Page Deep Dive

### 6.1 Home Landing Page (`src/pages/Home.tsx`)
The user's initial entrance. Surfaces market conditions immediately without requiring authentication.
- **Hero Strip**: Contains listing volume aggregates, real-time database counts, and the primary search bar. Submitting the search navigates users to `/listings?q=value`.
- **Market Snapshot Tile**: Pulls benchmark data (currently referencing *Ajah*) displaying Median Price, Price Reduction counts, and Average Days on Market with WoW comparison badges.
- **Trending Neighbourhoods**: Employs an `IntersectionObserver` sequence to lazy-load mini 4-week sparkline index charts (`Sparkline.tsx`) only when the card is scrolled into view.

### 6.2 Listings Search & Map View (`src/pages/Listings.tsx`)
A split-pane interface designed for desktop and mobile devices.
- **State & Filters**: Synced with URL search params via `useListingFilters.ts`. Includes search term, neighbourhoods, properties, bedrooms, price range, max listing age, and price reduction toggles.
- **Split Panes**:
  - **Left Pane**: Vertical grid of [ListingCard](file:///home/isla-jr/Documents/se-workspace/property-intel/client/src/components/listing/ListingCard.tsx) summaries. Features pagination ("Load More").
  - **Right Pane**: Dark CartoDB Leaflet map mapping geolocated properties.
- **State Synchronization**:
  - Hovering a `ListingCard` highlights its corresponding Leaflet pin.
  - Clicking a Leaflet pin highlights and smoothly scrolls the corresponding card into viewport focus.
- **Mobile Adaptive Layout**: Implements a floating toggle button to switch between full-screen listing grid views and full-screen map views, and loads the sidebar filters inside a bottom sheet modal.

### 6.3 Listing Detail Page (`src/pages/ListingDetail.tsx`)
Displays detailed analytics for a single property listing:
- **Primary Attributes**: Renders monospace numbers for bedroom counts, bathroom counts, and floor area metrics.
- **Price History Timeline**: A vertical timeline mapping structural changes (Listed, Relisted, Price Reduced/Increased, Removed) with exact naira difference markers. If the property has 4 or more price change events, it renders a Recharts area trend line chart.
- **Sidebar Analytics**: Houses a static, non-interactive Leaflet minimap preview centered on the property coordinates, alongside a neighbourhood context card comparing the property's price against regional percentiles (P10 to P90).

### 6.4 Market Overview Page (`src/pages/Market.tsx`)
Presents a grid layout of all tracked Lagos neighbourhoods. Calculates market-wide averages across active listings and showcases median index curves. Clicking a neighbourhood card redirects the user to its detail page.

### 6.5 Neighbourhood Detail Page (`src/pages/Neighbourhood.tsx`)
A data-heavy dashboard for a single neighbourhood (e.g. `Lekki Phase 1`):
- **Pricing Indicators**: Showcases active listings, average days on market, price reduction frequencies, and WoW trends.
- **Trend Chart**: A dual-axis Recharts visualization plotting the 12-week median price curve alongside active supply volumes.
- **Distribution Curve**: Implements the `PricePercentilesBar` showing price distribution, paired with a plain-text interpretation.

### 6.6 Search Alerts Dashboard (`src/pages/Alerts.tsx`)
A user-facing portal containing active email subscriptions. Access is protected by `ProtectedRoute.tsx`.
- **Alert Creation**: Users can define criteria (type, budget boundaries, target area) and choose email frequencies.
- **Price Conversion Constraint**: Form values are configured in Naira. During subscription payloads creation, maximum thresholds are multiplied by 100 on the client to match the backend database representation (which stores prices in Kobo).
- **Idempotent Actions**: Generates a client-side UUID key for each alert to prevent duplicate requests on network retries.
- **Friction Confirmation**: Deleting an alert triggers a 3-second inline toggle countdown ("CONFIRM DELETE?") to prevent accidental clicks.

---

## 7. Composite Components

### 7.1 Map View Component (`src/components/listing/MapView.tsx`)
Uses Leaflet (via React Leaflet) with Leaflet Cluster groups.
- **Robust Geodata Filtering**: Before passing coordinates to Leaflet, `MapView` filters out listings with missing geolocation parameters:
  ```typescript
  const validListings = listings.filter(l => l.lat !== null && l.lng !== null);
  ```
  This prevents Leaflet runtime crashes from properties with empty coordinate values.
- **Visuals**: Markers are rendered as custom amber SVGs. Marker clusters display a solid circular count matching the terminal's theme colors.

### 7.2 Custom Toast Alert Manager (`src/components/toast/ToastProvider.tsx`)
A context-driven notification manager rendering alerts in the bottom-right corner.
- **Queue Bounds**: Limit active toast counts to 3, queuing subsequent messages.
- **Cues**: Success indicators show a green stripe (`--color-signal-up`); error notifications show a red stripe (`--color-signal-down`) and persist until manually dismissed.

---

## 8. UX, Accessibility, & Accessibility Standards

### 8.1 Key UX Guardrails
- **No Chatbots/Decorative Blobs**: Background animations and floating conversation avatars are blocked. Surfaces remain flat and typographic.
- **Safe Truncation**: Text overflows are managed with CSS ellipsis variables (`.truncate-ellipsis`) alongside standard title HTML attributes to reveal full names on hover.
- **Data Placeholders**: Empty or missing data points are represented using an em dash (`—`) styled in `--color-text-tertiary`, rather than defaulting to `0` or `N/A`.

### 8.2 Keyboard Navigation
- Interactive listing cards and stats cards carry active roles (`role="button"`) and standard indexing (`tabIndex={0}`).
- Pressing `Space` or `Enter` triggers navigation identical to mouse clicks.
- Interactive modal sheets (mobile drawers, filter panels) trap keyboard focus.

### 8.3 Screen Reader Compatibility
- All icon-only interactive controls (like the close icon button `X`) carry descriptive `aria-label` tags.
- Decorative labels are hidden from screen readers using `aria-hidden="true"`.
- Toast notifications are loaded inside polite status announcement wrappers (`aria-live="polite"`).

### 8.4 Reduced Motion Compatibility
To assist users with vestibular sensitivities, `globals.css` overrides transitions and animations when `prefers-reduced-motion: reduce` is active:
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 9. Local Configuration & Run Script

### Development Proxy (`vite.config.ts`)
To prevent browser CORS blocks during development, `vite.config.ts` proxies `/api` queries to the backend running on port 8080:
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false,
    },
  },
}
```

### Environment Configuration (`.env`)
- **VITE_API_BASE_URL**: Set to `/` in local development environments. This redirects API queries through the local Vite dev proxy. For production builds, it is set to the deployed backend domain.
