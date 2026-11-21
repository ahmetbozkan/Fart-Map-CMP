# Skill: Repository and Mappers

## Purpose
Implement repository contracts and mapping layers without leaking DTO/entity types upward.

## Sources
- `planning/architecture/RepositoryRules.md`
- `planning/architecture/DataModel.md`
- `planning/architecture/DataContracts.md`

## Procedure
1. Define/confirm repository interface in domain.
2. Implement repository in data package.
3. Add explicit domain<->local and domain<->remote mappers.
4. Keep local-first write behavior.
5. Return typed results/errors.

## Failure recovery
- Missing required field mapping: block task and add mapping regression tests.

## Validation
- [ ] Domain imports no data entities
- [ ] Mapping round-trip tests added
- [ ] Visibility and privacy fields preserved
