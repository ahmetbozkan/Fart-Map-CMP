package com.ahmetbozkan.fartmap.core.di

import org.koin.core.module.Module

/** Bindings that need the platform (file paths, Context). Included by [sharedModules]. */
internal expect val platformModule: Module
