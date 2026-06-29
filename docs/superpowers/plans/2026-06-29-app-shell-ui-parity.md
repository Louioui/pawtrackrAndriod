# App Shell UI Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish the adaptive app navigation shell so screen-to-screen movement shares the Pawtrackr brand language.

**Architecture:** Keep the existing compact bottom bar and non-compact navigation rail because the current Material3 dependency set does not include `NavigationSuiteScaffold`. Localize destination labels, add a rail brand mark, and use Pawtrackr semantic colors without changing destination state or ViewModel wiring.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, existing window-size-class handling, Gradle debug build and JVM tests.

---

## File Structure

- Modify `app/src/main/java/com/example/pawtrackr/ui/PawtrackrApp.kt`: branded rail/header, localized labels, selected colors, stable navigation surfaces.
- Modify `app/src/main/res/values/strings.xml`: app title capitalization and nav labels.

## Task 1: App Shell Visual Pass

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/PawtrackrApp.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Localize destination labels**

Replace hardcoded enum label strings with string resource ids. Keep destination enum and selection state unchanged.

- [ ] **Step 2: Restyle navigation surfaces**

Use Pawtrackr brand colors for selected indicators/content. Add a compact rail brand mark for medium/expanded layouts. Keep bottom navigation on compact width.

- [ ] **Step 3: Verify**

Run: `./gradlew testDebugUnitTest assembleDebug --console=plain`

Expected: SUCCESS.

## Task 2: Commit Batch

**Files:**
- Stage only the plan, app shell, and strings files.

- [ ] **Step 1: Inspect status**

Run: `git status --short`

Expected: planned files plus pre-existing untracked `.idea/inspectionProfiles/`.

- [ ] **Step 2: Commit**

Run:

```bash
git add docs/superpowers/plans/2026-06-29-app-shell-ui-parity.md \
  app/src/main/java/com/example/pawtrackr/ui/PawtrackrApp.kt \
  app/src/main/res/values/strings.xml
git commit -m "feat: polish app shell navigation"
```

Expected: commit succeeds and `.idea/inspectionProfiles/` remains untracked.
