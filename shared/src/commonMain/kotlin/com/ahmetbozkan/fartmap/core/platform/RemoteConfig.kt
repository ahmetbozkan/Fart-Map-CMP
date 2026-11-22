package com.ahmetbozkan.fartmap.core.platform

/** A typed Remote Config key with the safe in-app default used whenever no fetched value exists (ADR-0024). */
class RemoteConfigKey<T : Any>(val name: String, val default: T)

/**
 * Runtime levers (ADR-0024). Values the server enforces (the rewarded-ad cap) are display-only here; the enforcing
 * Cloud Function reads the server-side template. Never gate an entitlement or unlock on a client value.
 */
object RemoteConfigKeys {
    /** Display-only: the backend enforces the real cap (Monetization.md §6). */
    val RewardedAdDailyCap = RemoteConfigKey("rewarded_ad_daily_cap", 3L)
    val StarterPackOfferEnabled = RemoteConfigKey("starter_pack_offer_enabled", true)
    val OfferingId = RemoteConfigKey("offering_id", "default")

    val all: List<RemoteConfigKey<*>> = listOf(RewardedAdDailyCap, StarterPackOfferEnabled, OfferingId)
}

/** Reads runtime levers. The Firebase-backed adapter arrives with its first consumer in EPIC-01-06. */
interface RemoteConfig {
    fun <T : Any> get(key: RemoteConfigKey<T>): T
}

/** Serves the in-app defaults; also the fallback the Firebase adapter delegates to. */
class DefaultsRemoteConfig : RemoteConfig {
    override fun <T : Any> get(key: RemoteConfigKey<T>): T = key.default
}
