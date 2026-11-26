import { logger } from "firebase-functions";

// Keys whose values must never reach logs (planning/architecture/Observability.md §5).
const FORBIDDEN_KEYS = new Set([
  "lat", "lng", "latitude", "longitude", "private_lat", "private_lng", "public_lat", "public_lng",
  "token", "fcm_token", "idToken", "accessToken", "refreshToken", "api_key", "apiKey",
  "description_text", "body",
]);

export type LogContext = Record<string, unknown>;

/** Returns a shallow copy with forbidden keys replaced by "[redacted]". */
export function redact(context: LogContext): LogContext {
  const out: LogContext = {};
  for (const [key, value] of Object.entries(context)) {
    out[key] = FORBIDDEN_KEYS.has(key) ? "[redacted]" : value;
  }
  return out;
}

export function logInfo(event: string, context: LogContext = {}): void {
  logger.info(event, redact(context));
}

export function logError(event: string, context: LogContext = {}): void {
  logger.error(event, redact(context));
}
