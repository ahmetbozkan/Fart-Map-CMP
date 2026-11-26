# Observability: Fart Map

> **Planning spine position:** authoritative error, logging, analytics, and operational telemetry contract.
> Upstream: `architecture/Architecture.md`, `architecture/Security.md`, ADR-0004.
> Downstream: error models, logging adapters, analytics emission points, alerting dashboards.
> This document defines how failures and key behaviors are observed. It does not define product KPIs (see `product/Metrics.md`).

---

## 1) Purpose

This document ensures the system is debuggable and safe in production by making failures explicit and observable.

Primary goals:

- no swallowed errors,
- typed failure propagation,
- privacy-safe telemetry,
- actionable operational signals for sync/auth/data issues.

---

## 2) Core Observability Principles

1. Every recoverable failure becomes typed app state or typed result.
2. Every important failure emits structured telemetry.
3. No sensitive payload leakage in logs/events.
4. Observability channels are separated by purpose.

---

## 3) Required Channels

1. **Application logs** (debug/diagnostic, structured, redacted).
2. **Error events** (operational failures with category and context) and **crash reports** via Firebase Crashlytics (ADR-0015), with custom keys limited to feature, operation and error category.
3. **Product analytics events** (behavioral signals, privacy-filtered).
4. **Security-sensitive audit events** (restricted access, stronger retention controls). *Phase 1 sizing for a solo owner: emit these as structured Cloud Functions logs with an `audit=true` label; a separate store with its own retention is deferred until there is an ops team or a compliance requirement.*

Do not merge these into one unstructured stream.

---

## 4) Error Modeling Contract

### Typed error model

Use shared typed errors (for example sealed error hierarchy) with stable categories such as:

- `NetworkUnavailable`,
- `Timeout`,
- `PermissionDenied`,
- `ValidationFailed`,
- `Conflict`,
- `AuthExpired`,
- `Unknown`.

Repositories and use cases return typed outcomes (`AppResult<T>`, `core/observability`), not raw exception propagation.

### ViewModel contract

- ViewModel state includes explicit error states where needed.
- One-time error effects (snackbar/toast/navigation) use event channels.
- `printStackTrace()` and silent catches are prohibited.

---

## 5) Logging Contract

### Structured fields (minimum)

- `event_name`,
- `severity`,
- `feature`,
- `operation`,
- `error_category` (if failure),
- `correlation_id` or operation id,
- `timestamp`.

### Forbidden log data

- secrets/tokens/credentials,
- precise coordinates,
- raw private UGC text by default,
- raw audio URLs with sensitive query params,
- payment receipt payloads,
- moderation reporter identity in general logs.

Redaction happens at source before emission.

---

## 6) Analytics Event Rules

1. Analytics captures behavioral milestones, not sensitive payload content.
2. Use stable event naming and parameter schema owned centrally in `core/observability`.
3. Never use analytics as authority for permissions, entitlements, or ranking.
4. Location analytics use coarse bins/keys only.

---

## 7) Sync and Auth Telemetry Requirements

### Write telemetry (required; ADR-0022)

- pending write acknowledged: time-to-ack bucket and whether it was queued offline,
- pending write rejected: operation and sanitized error category (from `PendingWriteMonitor`),
- count of writes still pending at app background (bucketed).

### Monetization and notification telemetry (required, Phase 1)

- purchase flow: offering shown, purchase started, purchase succeeded/failed/cancelled (category only, no receipt payloads),
- rewarded ad: sheet shown, ad requested, ad completed, unlock token issued/consumed/expired, daily cap reached,
- entitlement mirror lag (webhook to Firestore) bucketed,
- push: token registered/refreshed/removed, notification sent (server), notification opened (deep link resolved).

### Auth telemetry (required)

- sign-in start/success/failure (category only),
- token/session expiration transitions,
- sign-out transitions and cache-clear trigger outcomes.

---

## 8) Severity and Escalation Guidelines

- `DEBUG`: local diagnostics only, disabled or sampled in production.
- `INFO`: expected lifecycle milestones.
- `WARN`: recoverable anomalies needing attention.
- `ERROR`: operation failure affecting user flow or data correctness.
- `CRITICAL`: security/privacy risk or data-loss risk; immediate escalation.

Critical-class events require explicit escalation path per execution guardrails.

---

## 9) Testing Contract

Minimum observability tests:

1. caught exception maps to typed error state/event,
2. repository failure emits structured error event with sanitized fields,
3. forbidden-sensitive fields are redacted/absent,
4. sync terminal failures emit required telemetry,
5. auth-expired path emits session transition event and user-visible state.

---

## 10) Anti-Patterns (Forbidden)

- `printStackTrace()` as primary error handling.
- Empty catch blocks.
- Logging raw exceptions without context classification.
- Logging sensitive content for convenience debugging.
- Analytics event sprawl with inconsistent naming.

---

## 11) Phase 1 Analytics Event Taxonomy

The minimum event set that makes `Metrics.md` §4–§8 measurable in Phase 1. Names are stable; parameters carry ids and categories only, never free text, precise coordinates or tokens. Owned by `core/observability`.

| Event | When | Parameters | Metric served |
|---|---|---|---|
| `app_open` | cold/warm start | `session_kind` (anonymous/linked), `is_first_open` | Activation funnel |
| `onboarding_completed` | first-launch flow finished | `duration_bucket` | Activation |
| `map_viewed` | Map tab shown, after its first result | `marker_count_bucket` (`0`/`1_9`/`10_49`/`50_plus`, markers in view), `filter` (mine/followers/everyone) | L1/L3 health |
| `log_started` | Log Fart opened | `entry` (fab/quick) | L1 |
| `log_abandoned` | Log Fart dismissed without submit | `step_reached` | L1 leak rate |
| `log_submitted` | submit tapped | `visibility`, `has_description`, `has_sound`, `avatar_is_free`, `offline` | Activation, L1 |
| `marker_appeared` | own marker rendered after submit | `time_to_marker_bucket` | Time-to-first-log |
| `marker_opened` | marker tapped / detail sheet opened (the same tap once the sheet exists, EPIC-01-05; tracked once) | `is_own`, `source` (map/social_tab/push) | L2/L3 |
| `reaction_given` / `reaction_removed` | reaction tapped | `type`, `is_own_fart` | First social touch, L2 |
| `reaction_received` | server-side, on fan-out | `type` | L2 reciprocity |
| `fart_deleted` | delete confirmed | `age_bucket` | Trust |
| `report_submitted` / `block_applied` | safety action | `target_type`, `reason_code` | Counter-metrics (§8) |
| `account_link_prompted` / `account_linked` | nudge shown / link success | `provider` | Identity, retention |
| `notification_permission_prompted` / `notification_permission_result` | in-context prompt | `granted` | Notification trust |
| `location_permission_result` | a foreground location request ("center on me") ended with a decision; not sent when already granted, already blocked, or undecided | `result` (granted/denied/blocked) | Location trust, map activation |
| `notification_opened` | deep link resolved | `type` | Retention via social trigger |
| `cosmetic_sheet_shown` | locked item tapped | `item_type`, `options` (buy/ad) | Monetization funnel |
| `purchase_started` / `purchase_completed` / `purchase_failed` | store flow | `item_type`, `result_category` | Conversion |
| `rewarded_ad_requested` / `rewarded_ad_completed` / `rewarded_unlock_consumed` | ad flow | `item_type` | Ad funnel, cap monitoring |
| `write_rejected` | `PendingWriteMonitor` sees a server rejection | `operation`, `error_category` | Correctness |
| `share_created` / `share_link_opened` | share sheet completed / landing page or app link opened | `surface` | Virality (ADR-0023) |
| `wardrobe_opened` / `starter_offer_shown` / `starter_offer_result` | shop and offer funnel | `result_category` | Conversion (ADR-0024) |

Funnel transitions in `Metrics.md` §7 are derived from these events; no additional events are added without updating this table.

## 12) Review Checklist

- [ ] All failure paths map to typed outcomes.
- [ ] Structured telemetry emitted for operationally important failures.
- [ ] Sensitive data redaction rules are enforced.
- [ ] Sync/auth telemetry requirements are implemented.
- [ ] No swallowed errors remain in state flow paths.

---

*This is the observability contract. If failures are not typed and visible, the implementation is incomplete.*
