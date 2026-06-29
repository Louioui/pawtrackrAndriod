# Settings Onboarding UI Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bring Settings and Onboarding onto the Pawtrackr visual system so the full first-run and configuration flow matches the rest of the Android UI parity pass.

**Architecture:** Keep `SettingsViewModel` and `OnboardingViewModel` unchanged. Restyle only the Compose screens and add small string resources for new visible labels. Reuse existing shared components from `ui/components` and semantic tokens from `ui/theme`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, existing ViewModel state flows, Gradle debug build and JVM tests.

---

## File Structure

- Modify `app/src/main/java/com/example/pawtrackr/ui/settings/SettingsScreen.kt`: card-based profile form, status chips, semantic save/error feedback.
- Modify `app/src/main/java/com/example/pawtrackr/ui/onboarding/OnboardingScreen.kt`: branded welcome card and card-based business setup form.
- Modify `app/src/main/res/values/strings.xml`: add small Settings/Onboarding helper labels.

## Task 1: Settings Visual Pass

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Replace flat form with Pawtrackr cards**

Use `PawtrackrCard`, `PawtrackrSectionTitle`, and `PawtrackrChip` around the existing fields. Preserve every ViewModel setter, validation flag, save state, and `save()` call.

- [ ] **Step 2: Compile**

Run: `./gradlew assembleDebug --console=plain`

Expected: SUCCESS.

## Task 2: Onboarding Visual Pass

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/onboarding/OnboardingScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Replace emoji-only welcome with branded Pawtrackr card**

Use Pawtrackr brand colors, cards, chips, and a compact visual identity surface. Preserve `viewModel::next`.

- [ ] **Step 2: Restyle business form**

Use `PawtrackrCard`, semantic feedback, and the same field behavior. Preserve `onFinish`, `onBack`, saving spinner, and validation flags.

- [ ] **Step 3: Full verification**

Run: `./gradlew testDebugUnitTest assembleDebug --console=plain`

Expected: SUCCESS.

## Task 3: Commit Batch

**Files:**
- Stage only the plan, Settings, Onboarding, and string resource changes.

- [ ] **Step 1: Inspect status**

Run: `git status --short`

Expected: planned files plus pre-existing untracked `.idea/inspectionProfiles/`.

- [ ] **Step 2: Commit**

Run:

```bash
git add docs/superpowers/plans/2026-06-29-settings-onboarding-ui-parity.md \
  app/src/main/java/com/example/pawtrackr/ui/settings/SettingsScreen.kt \
  app/src/main/java/com/example/pawtrackr/ui/onboarding/OnboardingScreen.kt \
  app/src/main/res/values/strings.xml
git commit -m "feat: polish settings and onboarding UI"
```

Expected: commit succeeds and `.idea/inspectionProfiles/` remains untracked.
