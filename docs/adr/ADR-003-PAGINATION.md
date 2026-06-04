# ADR-003: Cursor-Based Pagination Over Offset

> Date: 2026-05-28
> Status: Accepted

## Context

Offset pagination has proven to lead to two failure modes at scale. First, performance: `OFFSET 1000` forces the database to scan and discard 1000 rows before returning results. This means that cost grows linearly with offset. Second, correctness: concurrent writes during pagination cause duplicates (a new listing inserted before cursor position re-appears on the next page) or skips (a listing deleted after retrieval is never seen). For a dataset receiving weekly scraper additions, this may not be a major problem, but cursor pagination is still an evidently superior option.

## Decision

All endpoints requiring pagination use cursor-based pagination (`WHERE id > :cursor ORDER BY id LIMIT N`) rather than offset pagination (`LIMIT N OFFSET M`).

## Consequences

No real tradeoffs exist for this decision. It should, however, be revisited if a use case emerges requiring random page access that cannot be satisfied by filtering.
