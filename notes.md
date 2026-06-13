* F-Droid Deployment Playbook & Lessons Learned

This document serves as a post-mortem and deployment guide for publishing hybrid (Capacitor/Cordova) Android applications to the official F-Droid repository. 

It details the structural and architectural edge cases encountered while passing the F-Droid CI matrix.

1. The Capacitor + F-Droid Build Paradigm

# The Problem

F-Droid requires 100% offline compilation from source. Hybrid frameworks like Capacitor typically rely on dynamic Node/NPM dependencies and localized web asset builds, which conflict with F-Droid's sandboxed build environments.

# Lessons Learned

## Offline Asset Vendoring 

Web assets (HTML, JS, CSS) must be fully compiled and injected into the native `android/app/src/main/assets/public directory` before tagging a release. F-Droid cannot execute network-dependent JavaScript bundle runners during its build cycle.

## Build Target Auto-Detection

F-Droid prefers convention over explicit overrides. Do not hardcode manual `output: APK targets` in the metadata recipe. Instead, point the subdir to the actual module engine context.

2. The Cloud Linter Mismatch & JSON Schema Catch-22


# The Problem

The stable versions of fdroidserver distributed in typical Linux package managers (e.g., Mint/Debian repositories) are frozen. 

The F-Droid cloud runner, however, uses the absolute latest development master branch. This results in local format updates being rejected by the cloud due to hidden formatting logic and structural changes : Local Format Pass  ➡️  Cloud Re-sort Loop  ➡️  Git Diff Mismatch  ➡️  Pipeline Failure

# Lessons Learned

## Sync Local Tools with Upstream

Never use distribution packages for metadata validation. Clone the bleeding-edge fdroidserver source code locally to ensure zero-diff matches against the GitLab CI runners:

```git clone https://gitlab.com/fdroid/fdroidserver.git```

## Version Property: 

The cloud runs an isolation test pass using check-jsonschema that strictly requires CurrentVersion and CurrentVersionCode properties to exist at the root level. Standalone formatting commands (fdroid rewritemeta) will scrub these values out if the local session cache is cold.

## Pre-Format Workflows

Always hydrate your tool's session database using checkupdates before re-indexing file formatting layouts:

```
fdroid checkupdates --allow-dirty <app_id>
fdroid rewritemeta <app_id>
```

3. Legacy Tag Traps & Regex Filtering

# The Problem

The fdroid checkupdates engine recursively parses your entire upstream Git tag history to determine incremental progress. 

If early development tags (e.g., old alpha or baseline test commits) contain broken Gradle files or missing modules, the Python tracker crashes mid-run with a `TypeError: '>' not supported between instances of 'int' and 'NoneType'`.

```UpdateCheckMode: Tags ^v1\.0\.(10|9|8|7)$```

4. F-Droid Structural Best Practices (The Reviewer Rules)

To ensure rapid human review and immediate pipeline merging, adhere strictly to these structural constraints

| Requirement | Metadata Strategy | Purpose |
| :--- | :--- | :--- |
| **Immutability Check** | Use the full **Commit SHA** in the build block instead of a tag string name (`commit: c341e...`) | Secures the supply chain. Prevents a developer from maliciously or accidentally changing the underlying code of an existing tag after the code review is complete. |
| **Single Source of Truth** | **Omit** the `Description:` or `Summary:` block from the `.yml` recipe file | F-Droid mandates that application marketing text must live upstream inside the app repo's `fastlane/metadata/android/` path. Keeping it out of the recipe prevents metadata duplication. |
| **Convention over Output** | Set `subdir: android/app` and completely remove any explicit `output:` lines | Shifts the working directory directly to the app module context. This lets the F-Droid harvester auto-detect standard generated release binaries without manual tracking. |

5. Instant Local Verification Cycle

When prepping a new release or a new application, execute this precise CLI sequence to guarantee a green pipeline on the first push:

```
# 1. Drop into the local metadata workspace
cd ~/development/android/fdroid-fork/fdroiddata

# 2. Sync version caching records from remote tags
fdroid checkupdates --allow-dirty io.alain.shorthander

# 3. Apply canonical layout constraints
fdroid rewritemeta io.alain.shorthander

# 4. Verify there are zero local formatting mutations left to surprise you in the cloud
git diff metadata/io.alain.shorthander.yml

```