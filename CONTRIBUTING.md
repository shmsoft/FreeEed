# Contributing

This describes the branch workflow used across the three FreeEed repos
(FreeEed, FreeEedUI, ai_advisor). It is the same in each.

## Branches

- **`dev`** — the shared integration branch. All builds and releases are cut
  from `dev`, and collaborators merge their work here (PRs, doc updates, etc.).
- **`mark`** — Mark's working branch. Day-to-day development happens here.
- **`main`** — public branch. Do not merge to it unless explicitly asked.

## The flow

Develop on `mark`, build/release from `dev`:

1. Do your work on `mark`.
2. Before building on `dev`, merge **`mark → dev`** to publish your work.
3. Regularly — and before resuming on `mark` — merge **`dev → mark`** to absorb
   everyone else's commits.

**Sync both ways, and sync often.** `dev` is shared, so it advances from other
people's commits, not just from `mark`. If you treat `mark` as the only feeder
for `dev`, `mark` goes stale and merges get painful (this has bitten us before
— an 11-commit drift). The merge cost is proportional to how stale `mark` gets.

## Rules

- Prefer fast-forward merges.
- Never force-push a shared branch (`dev`, `main`) without explicit agreement.
- Builds are identified by git commit SHA + build time (see the About dialog,
  `-version`, and the pack `VERSION` file), so the semantic version only needs
  to be bumped at milestones, not per build.

## External contributors & interns

If you are not a repo maintainer, work on a `feature/<short-description>` branch
(or a fork) and open a **pull request into `dev`**. Do **not** push directly to
`mark`, `dev`, or `main`. See **`docs/intern-onboarding.md`** for a step-by-step
setup, and **`CLAUDE.md`** for how the project is built and how we work with Claude.

## Contributor License Agreement

Contributions are accepted under a Contributor License Agreement — see **`CLA.md`**.
Sign it before your first PR is merged; it clarifies IP/licensing so your work ships
cleanly under Apache-2.0.
