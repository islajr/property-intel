# Property Intelligence Platform — Frontend Client

A high-performance, dark-theme data intelligence dashboard designed for the Nigerian residential property market. Built as a desktop-first, mobile-responsive web application that communicates real-time price trend indices, neighbourhood aggregates, and listing flow dynamics in Lagos (including Lekki Phase 1, Ajah, Victoria Island, Ikeja GRA, etc.).

---

## ── Product context & Philosophy

This platform is a dedicated professional analytical tool rather than a standard consumer listing search engine or agency portal. In accordance with the **No-Photograph Constraint**, the visual layout is tailored as a financial data terminal—restraining visual noise, establishing high text-based information density, and presenting real-time statistical signals (e.g. price drops, days-on-market traffic lights) with extreme precision.

---

## ── Technology Stack

- **Core**: React 18 (TypeScript), Vite
- **Styling**: Vanilla CSS custom properties (`globals.css`) for high-fidelity control without utility framework bloat.
- **Routing**: React Router DOM (v6) with path-key transitions.
- **State Management**: 
  - **Zustand**: In-memory credential state mapping (never persisted on localStorage/cookies to prevent XSS leakage).
  - **React Query (Tanstack)**: Declarative, stale-time caching query layer.
- **Data Visualizations**: 
  - **Recharts**: Simplified 4-week trend sparklines and longitudinal price charts.
  - **Leaflet (React Leaflet)**: Map container utilizing CartoDB Dark Matter tile layer with custom amber SVG markers and marker clustering.

---

## ── Project Directory Layout

```text
client/
├── public/                 # Static asset public directory
├── src/
│   ├── api/                # Axios API service integrations & endpoints
│   ├── components/
│   │   ├── auth/           # Login/Register wrappers & Protected Route logic
│   │   ├── layout/         # Shell structures, Navigation Drawer, Filter Sheets
│   │   ├── listing/        # MapView, Listing Cards, detail sub-components
│   │   ├── market/         # Price percentiles, Sparklines, Stat cards
│   │   └── toast/          # Context-driven custom toast alerts
│   ├── hooks/              # Query state & browser interaction helpers
│   ├── pages/              # Routed view compositions (Home, Listings, alerts...)
│   ├── store/              # Zustand global credential memory stores
│   ├── styles/             # Global visual tokens, animations, and CSS resets
│   ├── types/              # TS interface shapes mirroring API responses
│   └── main.tsx            # App render mounting point
├── tsconfig.json           # App compiler targets
└── vite.config.ts          # Vite asset bundling rules
```

---

## ── Environment Variables

Configure application settings by copying `.env.example` to `.env`:

```bash
cp .env.example .env
```

| Key | Description | Example Value |
|---|---|---|
| `VITE_API_BASE_URL` | Base endpoint mapping for the Spring Boot API services | `http://localhost:8080` |

> [!IMPORTANT]
> The client handles monetary representations exactly as received from the backend: price formatting happens server-side (`price_formatted` returned as e.g. `₦45,000,000`). The only exception is the single Naira-to-kobo conversion factor handled on the Search Alert creation payload.

---

## ── Getting Started

### Prerequisites

Ensure you have [Node.js](https://nodejs.org/) (v18 or higher) and `npm` installed.

### 1. Install Dependencies
```bash
npm install
```

### 2. Configure Environment API Base
Verify that the `VITE_API_BASE_URL` in `.env` matches your local Java Spring API target or live Railway deployment.

### 3. Launch Development Server
```bash
npm run dev
```
The application will boot locally at [http://localhost:5173/](http://localhost:5173/).

### 4. Build for Production
Generate optimized, minified production assets in the `dist/` directory:
```bash
npm run build
```

---

## ── Core Design Standards

- **Restrained Lagos Context**: Accent highlights use warm Lagos amber (`var(--color-amber-400)`), overlaying slate-ink bases. Pure white is avoided to reduce screen glare.
- **Monospace Typography**: All statistical figures, currencies (₦), days-on-market counts, and tables use the monospace DM Mono font (`var(--font-numeric)`) to guarantee vertical column alignment.
- **Keyboard Tab-trapping**: Modals, filter bottom sheets, and drawers trap tab navigation correctly. Clickable listing grid cards and stats cards carry native `role="button"`, `tabIndex={0}`, and trigger navigation on `Space`/`Enter` press.
- **Empty States**: Views feature descriptive, two-part empty states (explaining why a viewport is empty and providing a clear path forward).
- **Reduced Motion Support**: An animation override is configured inside `globals.css` targeting `prefers-reduced-motion: reduce` to immediately zero out slide-ups and transitions.
