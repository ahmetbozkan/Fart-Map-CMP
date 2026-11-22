// shared: feature module (core/di or feature/<name>/di)
package com.ahmetbozkan.fartmap.feature.example.di

import com.ahmetbozkan.fartmap.feature.example.presentation.ExampleViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val exampleModule = module {
    viewModelOf(::ExampleViewModel)
}

// firebaseData: bind the implementation to the domain interface
// val exampleDataModule = module {
//     single<ExampleRepository> { ExampleRepositoryImpl(get(), get()) }
// }
// Startup composes sharedModules + firebaseDataModules + platformModule (Android: Application; iOS: umbrella).
