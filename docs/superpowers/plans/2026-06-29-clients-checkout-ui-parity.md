# Clients Checkout UI Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade Clients and Checkout to use the Pawtrackr iOS-inspired visual system while preserving current Room, ViewModel, checkout math, messaging, CRUD, and photo behavior.

**Architecture:** Keep `ClientsViewModel` and `CheckoutViewModel` as the behavior owners. Add two reusable visual primitives under `ui/components`, then restyle `ClientsScreen.kt` and `CheckoutSheet.kt` in place to limit blast radius. Add string resources for new labels touched during the refactor.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, existing Room/ViewModel flows, existing JVM test suite, Gradle debug build.

---

## File Structure

- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrTimeline.kt`: timeline row for visit/history cards.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrPhotoWell.kt`: before/after photo well surface for checkout and visit history.
- Modify `app/src/main/java/com/example/pawtrackr/ui/clients/ClientsScreen.kt`: use shared Pawtrackr cards/chips/search/FAB/stat/timeline/photo primitives and improve visual hierarchy.
- Modify `app/src/main/java/com/example/pawtrackr/ui/checkout/CheckoutSheet.kt`: convert dense bottom sheet body to stepped sections while keeping current state and actions.
- Modify `app/src/main/res/values/strings.xml`: add Clients strings and new Checkout step strings.

## Task 1: Shared Timeline And Photo Components

**Files:**
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrTimeline.kt`
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrPhotoWell.kt`

- [ ] **Step 1: Add `PawtrackrTimelineItem`**

Create a stateless composable with a colored node, optional connector, title, subtitle, and trailing content. Use `PawtrackrTokens` spacing and semantic colors.

- [ ] **Step 2: Add `PawtrackrPhotoWell`**

Create a stateless composable that supports a label, optional bitmap bytes, optional selected state, and click action. Decode thumbnails with `BitmapFactory` inside `remember(bytes)`.

- [ ] **Step 3: Compile**

Run: `./gradlew assembleDebug --console=plain`

Expected: SUCCESS.

## Task 2: Clients List And Detail Visual Pass

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/clients/ClientsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Replace list controls**

Use `PawtrackrSearchField`, `PawtrackrChip`, and a compact sort text button. Add string resources for visible Clients labels touched by the edit.

- [ ] **Step 2: Restyle client rows**

Use `PawtrackrCard` with leading accent rails, initials avatar, status chips, contact summary, pet count, safety icon/text, and selected-state container tint.

- [ ] **Step 3: Restyle client detail**

Use a profile card, action chips/buttons, `PawtrackrKpiCard` stats, and Pawtrackr-styled pet cards. Preserve CRUD, messaging, and split-pane behavior.

- [ ] **Step 4: Restyle pet detail and visit history**

Use safety banners, behavior chips, metric cards, `PawtrackrTimelineItem`, and `PawtrackrPhotoWell` thumbnails. Preserve check-in/checkout actions and photo rendering.

- [ ] **Step 5: Build**

Run: `./gradlew assembleDebug --console=plain`

Expected: SUCCESS.

## Task 3: Checkout Stepped Visual Flow

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/checkout/CheckoutSheet.kt`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add internal checkout step enum**

Add a local enum or model in `CheckoutSheet.kt` for `SERVICES`, `DETAILS`, `PAYMENT`, and `REVIEW`. This is presentation-only and must not change `CheckoutViewModel`.

- [ ] **Step 2: Split dense content into section composables**

Create internal composables for service selection, custom total/photos, payment/tip, and review total. Use `PawtrackrCard`, `PawtrackrChip`, `PawtrackrPhotoWell`, and semantic colors.

- [ ] **Step 3: Preserve checkout behavior**

Keep service toggles, manual total, photo pickers, payment method/reference, tips, error display, processing state, and `confirm()` exactly wired to the current ViewModel.

- [ ] **Step 4: Full verification**

Run: `./gradlew testDebugUnitTest assembleDebug --console=plain`

Expected: SUCCESS.

## Task 4: Commit Batch

**Files:**
- Stage only the files touched by this plan.

- [ ] **Step 1: Inspect status**

Run: `git status --short`

Expected: planned files plus pre-existing untracked `.idea/inspectionProfiles/`.

- [ ] **Step 2: Commit**

Run:

```bash
git add docs/superpowers/plans/2026-06-29-clients-checkout-ui-parity.md \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrTimeline.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrPhotoWell.kt \
  app/src/main/java/com/example/pawtrackr/ui/clients/ClientsScreen.kt \
  app/src/main/java/com/example/pawtrackr/ui/checkout/CheckoutSheet.kt \
  app/src/main/res/values/strings.xml
git commit -m "feat: polish clients and checkout UI"
```

Expected: commit succeeds and `.idea/inspectionProfiles/` remains untracked.
