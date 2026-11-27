# Renovate automerge

## Current configuration

`cmc-claim-store` uses `.github/renovate.json`, extending the shared HMCTS preset and `local>hmcts/.github//renovate/automerge-all`, which sets `automerge: true` for every update, majors included.

Repo-level settings:

- `timezone`: `Europe/London`.
- `schedule` and `automergeSchedule`: after 7am and before 9am, and after 5pm and before 8pm, every weekday.
- `platformAutomerge`: `false`, so Renovate enforces the configured merge windows instead of queuing GitHub auto-merge.
- `rebaseWhen`: `behind-base-branch`, so Renovate refreshes branches that fall behind `master`.
- `automergeStrategy`: `squash`.
- `prConcurrentLimit`: `5`.

Package rules, in order:

| Rule | Matches | Automerge |
|---|---|---|
| All non-major updates | every minor and patch update not taken by a group below | yes |
| Pact | packages containing `pact` | disabled |
| Spring Boot and related updates | minor and patch of Spring, springdoc, Spring Cloud, Jakarta Persistence and Hibernate | yes |
| Terraform updates | `terraform` manager | yes |
| Terraform version | `infrastructure/.terraform-version` | no |
| Jenkins library | `hmcts/cnp-jenkins-library` | yes (from the preset) |
| Spring Boot hold | `org.springframework.boot` | below 4.2.0 |

### Group order

When several package rules match, Renovate applies them in order and later values win. "All non-major updates" must stay first, so each named group below it takes its own packages out of the catch-all. Renovate still raises majors on separate branches (for example `renovate/major-spring-cloud`).

Until this was fixed, "All non-major updates" came after the Spring and Terraform groups and replaced their `groupName`. Spring minor and patch updates all went into the "All non-major updates" PR, so the "Spring Boot and related updates" group never opened a PR, and the Terraform group only received majors.

### Major updates automerge

Nothing turns automerge off for majors except the Terraform version, so major updates automerge once their checks pass. That includes Spring majors such as Spring Cloud 2026 (#3577). `civil-service` keeps Spring majors from being raised at all until the platform is ready, and `civil-wa-task-configuration` holds Spring and Camunda majors for review.

## Code-owner review

`CODEOWNERS` assigns `*` to `@hmcts/pet`, then lists the files Renovate updates without an owner, so Renovate PRs that only touch them don't need code-owner review: `.pnp.cjs`, `.yarn/**`, `package.json`, `yarn.lock`, `charts/**/Chart.yaml`, `infrastructure/.terraform-version`, `infrastructure/state.tf`, and `.github/workflows/*.yaml` and `*.yml`.

`build.gradle`, the Gradle wrapper files, `Dockerfile` and `Jenkinsfile_CNP` are not in that list. Most Renovate PRs in this repository touch one of them, so they need `@hmcts/pet` approval before they can automerge. Add them to the ownerless block if those PRs should merge without review, as `civil-service` and `civil-wa-task-configuration` do.

## Merge window and Jenkins availability

PTL Jenkins runs on `cft-ptl-00-aks`, which [`hmcts/auto-shutdown`](https://github.com/hmcts/auto-shutdown) normally stops from 20:00 to 06:30 daily. There is no scheduled weekend startup.

If Renovate updates or rebases a PR while Jenkins is unavailable, the required check can't complete and automerge stays blocked. Branch creation and automerge therefore share the 07:00–09:00 and 17:00–20:00 weekday windows, when Jenkins is online and developer activity is lower. A PR rebased late in the evening window may not finish its build before the 20:00 shutdown; it then merges in a later window.

`platformAutomerge` must stay `false`; otherwise Renovate enables GitHub auto-merge and GitHub can merge whenever checks pass, regardless of `automergeSchedule`.

## Holds

| Package | Hold | In use | Reason |
|---|---|---|---|
| `org.springframework.boot` | below 4.2.0 | 4.1.3 | Spring Cloud 2025.1.x (2025.1.5 in use) only supports Boot 4.0.x and 4.1.x. Remove when moving to Spring Cloud 2026.0 |
| Pact packages | disabled | | no `description` |

When a Renovate PR fails the build on every rebase, find the cause and add a hold with its reason rather than leaving the PR open: with `rebaseWhen: behind-base-branch`, a PR that can never pass is rebuilt on every merge to `master`.

## Stalled-queue alert

`civil-service`'s `.github/workflows/renovate-stalled-alert.yml` also checks this repository's open Renovate PRs each weekday morning and reports any older than three days.

## Release freeze

Use a Renovate package rule for a planned release freeze, so the freeze is reviewable and recorded in the repository. Add it as the final item under `packageRules` in `.github/renovate.json`:

```json
{
  "description": "Release freeze: disable Renovate automerge",
  "matchPackageNames": ["*"],
  "automerge": false
}
```

It must stay last: later rules win, and several rules above set `automerge: true`. The rule stops automerge but leaves Renovate opening and rebasing PRs.

### Start a freeze

1. Add the freeze rule in a pull request, with the release or change reference and the intended end date in the description.
2. Merge it before the freeze starts, then run Renovate manually or wait for its next scheduled run.
3. Confirm in the Mend job log that the rule resolves to `automerge: false`, and record that in the release ticket.

### End a freeze

1. Remove the freeze rule through a pull request once the release is approved.
2. Merge it, then run Renovate manually or wait for its next scheduled run.
3. Check that a clean Renovate PR automerges only after all required checks pass.

## Pausing Renovate

Pausing is broader than a release freeze: it also stops new PRs, rebases and PR maintenance.

| | Release freeze (`automerge: false` rule) | Pause |
|---|---|---|
| New PRs and rebases | continue | stop |
| Automerge | stops | stops |
| Renovate's PR queue | stays current; merges are manual | builds up until turned back on |

There are two ways to pause:

- **In the repository:** set `"enabled": false` at the top level of `.github/renovate.json` in a pull request, with the reason and expected end. It takes effect once merged and Renovate next runs. Revert the pull request to turn Renovate back on.
- **In the Mend Developer Portal:** an organisation admin can pause the repository at `https://developer.mend.io/github/hmcts/cmc-claim-store/-/settings`. This takes effect without a merge, so it suits a freeze that has to start straight away. Turn it back on in the portal.

Changing branch protection is not recommended as a routine toggle: it affects all contributors and makes intentional freezes look like authorisation failures.
