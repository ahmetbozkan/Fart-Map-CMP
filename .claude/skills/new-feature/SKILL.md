---
name: new-feature
description: Scaffold or extend a feature slice in the KMP app (Screen, ViewModel with State/Action/Event, repository interface + fake, firebaseData implementation, Koin module, typed route, tests) following the project's single pattern. Use when adding a screen, a feature package, or a repository.
---

# New feature slice

The templates in `templates/` **are** the canonical pattern; copy them and rename. STORY-01-01-02 creates the base types they reference (`StateViewModel`, `AppError`, `AppResult`). If a template and the real base class disagree, update the template in the same PR.

## Steps
1. **Name it.** Feature package `feature/<name>` (`map`, `log`, `auth`, `profile`, `social`, `progression`, `monetization`, `notifications`). Shared types go in `core/domain`.
2. **Domain** (`shared/.../feature/<name>/domain/`):
   - entity data classes;
   - a `<Name>Repository` interface (`templates/Repository.kt`);
   - a use case only if it holds real logic.
3. **Presentation** (`shared/.../feature/<name>/presentation/`):
   - `<Name>State.kt` (State + Action + Event; `templates/FeatureState.kt`);
   - `<Name>ViewModel.kt` (`templates/FeatureViewModel.kt`);
   - `<Name>Screen.kt` (`templates/FeatureScreen.kt`: Route + stateless Screen + previews per state).
4. **Fake** (`shared/src/commonTest/.../feature/<name>/Fake<Name>Repository.kt`, `templates/FakeRepository.kt`).
5. **Tests** (`templates/FeatureViewModelTest.kt`): each Action → expected State/Event, including the error path.
6. **Firebase implementation** (`firebaseData/src/commonMain/.../firebase/<name>/<Name>RepositoryImpl.kt` + DTO + mapper; `templates/RepositoryImpl.kt`):
   - the mapper round-trips every field;
   - add a mapper test in `firebaseData` tests when it has a test source set, otherwise as pure functions tested in `shared`.
7. **DI:** add the ViewModel to the shared feature module and the `RepositoryImpl` binding to the firebaseData module (`templates/Module.kt`).
8. **Navigation:** add a `@Serializable` route in `core/navigation` and register the destination. Navigate from an Event, never from the ViewModel directly.
9. **Strings and design:** strings in `composeResources`; tokens from `core/design`. Implement every state from `planning/design/screens/<Screen>.md`.
10. **Verify:** `/verify`; both platforms build.

## Checks before done
- [ ] No Firebase, Room or `data` imports in domain or presentation.
- [ ] No other feature imported.
- [ ] Errors are typed: every `catch` maps to State/Event or the logger, and `CancellationException` is rethrown.
- [ ] Every screen-spec state has a preview.
- [ ] The visibility default and privacy rules hold for any new fart-related field.
