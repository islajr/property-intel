# ADR-002: Refresh Tokens in Http-Only Cookies

> Date: 2026-05-15
> Status: Accepted

## Context

Tokens stored in `localStorage` are readable by any JavaScript on the page, including injected scripts from XSS vulnerabilities. HttpOnly cookies cannot be read by JavaScript at all, as they are transmitted automatically by the browser to the same origin. For the short-lived access token (15 minutes), in-memory storage on the client is acceptable. For the long-lived refresh token (7 days), HttpOnly cookie storage eliminates the XSS attack possibility.

## Decision

Refresh tokens are stored in HttpOnly cookies and sent to `/api/v1/auth` endpoints only. Access tokens are returned in the response body and stored in memory by the client.

## Consequences

The refresh endpoint must handle the cookie correctly server-side. CORS configuration must permit cookie transmission for the frontend's origin. `SameSite=Strict` on the cookie provides additional CSRF protection.
