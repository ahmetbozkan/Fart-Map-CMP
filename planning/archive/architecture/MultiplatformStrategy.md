# Multiplatform Strategy: Fart Map

> **Planning spine position:** authoritative shared-vs-platform boundary contract.
> Upstream: `architecture/ArchitecturePrinciples.md`, `architecture/Architecture.md`, ADR-0003, ADR-0008.
> Downstream: module/package placement, expect/actual implementations, platform shell setup.
> This document defines how Android and iOS parity is achieved without leaking platform APIs into shared logic.

---

## 1) Purpose

This document sets strict multiplatform boundaries so behavior stays consistent across platforms while still allowing native integrations.

It prevents:

- platform API leakage into shared business code,
- feature parity drift,
- duplicate business logic implementations.

---

## 2) Core Principle

Shared by default, platform-specific by necessity.

Any code that can live in shared code must live in shared code.
Platform-specific code is allowed only when a platform API forces it.

---

## 3) Shared-Code Ownership

Shared module owns:

- feature presentation/domain/data logic,
- ViewModel state machines,
- repository contracts and implementations,
- sync engine behavior,
- navigation route models and graph contracts,
- design system tokens/components,
- observability contracts and typed errors.

---

## 4) Platform-Code Ownership

Platform modules own:

- `actual` implementations for shared `expect` contracts,
- SDK adapters (maps, location, auth providers, media, notifications),
- app shell bootstrap and platform lifecycle bridging,
- platform-specific permission adapters.

Platform code must not own business decisions or feature rules.

---

## 5) Expect/Actual Contract Rules

1. Shared layer defines minimal, explicit contracts (`expect` declarations or interfaces).
2. Each supported platform provides exactly one `actual` implementation per contract.
3. Contracts should expose domain-safe types, not raw platform SDK types.
4. No conditional platform branching in shared business code as a substitute for `expect`/`actual`.

---

## 6) Dependency Injection Across Platforms

Follow ADR-0003 pattern:

- shared module defines common DI graph,
- platform module contributes bindings for platform contracts,
- app startup composes shared + platform modules.

No platform singleton shortcuts that bypass DI boundaries.

---

## 7) Parity Rules

1. Feature behavior parity is required across Android and iOS.
2. UX can follow platform conventions when behavior stays equivalent.
3. A feature is not done if implemented on only one platform.
4. Known parity gaps must be tracked as defects, not informal TODOs.

---

## 8) Platform Capability Areas (Initial)

Platform-bound capabilities expected from start:

- map rendering,
- location providers,
- native auth provider hooks,
- audio recording and media file access,
- push notification plumbing (FCM/APNs token registration),
- secure storage primitives,
- rewarded ads adapter (Google Mobile Ads + UMP consent; ADR-0017),
- purchases client (RevenueCat KMP SDK; ADR-0016),
- App Check provider and Crashlytics initialization (ADR-0015).

Each capability must be wrapped by a shared contract before use by feature logic.

---

## 9) Testing Contract

1. Shared business logic tests run without platform SDK.
2. Platform contract tests verify `actual` implementations satisfy shared contract behavior.
3. Parity checks verify equivalent outcomes for critical user flows (map, log, auth, profile).
4. Regression tests required when a platform-only bug implies contract mismatch.

---

## 10) Anti-Patterns (Forbidden)

- Android/iOS SDK imports inside shared domain/data/business files.
- Copy-pasted business rules diverging between platforms.
- Platform-specific feature forks for non-platform-forced behavior.
- Platform code making product-policy decisions (visibility, monetization, safety rules).

---

## 11) Review Checklist

- [ ] New platform capability introduced via shared contract.
- [ ] `expect`/`actual` boundary is explicit and minimal.
- [ ] Shared logic remains platform-agnostic.
- [ ] Behavioral parity validated for affected features.
- [ ] DI wiring uses shared + platform modules pattern.

---

*This is the multiplatform boundary contract. Any platform leakage into shared business logic is an architecture defect.*
