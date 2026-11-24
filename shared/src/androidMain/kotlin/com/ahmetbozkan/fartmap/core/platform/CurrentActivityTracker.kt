package com.ahmetbozkan.fartmap.core.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * The resumed Activity, for platform UIs that need one (the Credential Manager sheet). Registered once from the
 * Koin Application context, so the shell and the ViewModels never pass a Context around. Weak, so it never leaks.
 */
internal class CurrentActivityTracker(application: Application) : Application.ActivityLifecycleCallbacks {
    private var current: WeakReference<Activity>? = null

    val activity: Activity? get() = current?.get()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (current?.get() === activity) current = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
