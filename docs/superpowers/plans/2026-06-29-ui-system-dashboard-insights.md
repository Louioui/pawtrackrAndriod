# UI System Dashboard Insights Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first production UI parity slice by adding Pawtrackr Compose design primitives and applying them to Dashboard and Insights.

**Architecture:** Keep Room, repositories, and ViewModels unchanged. Add a small UI design-system layer under `com.example.pawtrackr.ui.components` and expand `com.example.pawtrackr.ui.theme` with iOS-derived tokens. Dashboard and Insights consume those primitives so later Clients and Checkout work can reuse the same visual language.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, JUnit 4 for JVM token-contract tests, existing Gradle/AGP setup.

---

## File Structure

- Create `app/src/main/java/com/example/pawtrackr/ui/theme/PawtrackrDesign.kt`: source of truth for brand colors, semantic accents, spacing, radii, and elevation constants.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrCard.kt`: reusable card with optional leading/top accent rails.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrChip.kt`: reusable compact semantic chips.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrSearchField.kt`: capsule search field for later Clients work.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrFab.kt`: branded extended FAB helper for later Clients work.
- Create `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrMetrics.kt`: KPI card, section title, and bar-list helpers shared by Dashboard/Insights.
- Modify `app/src/main/java/com/example/pawtrackr/ui/theme/Theme.kt`: align fallback palette to Pawtrackr brand while preserving Material You dynamic color.
- Modify `app/src/main/java/com/example/pawtrackr/ui/dashboard/DashboardScreen.kt`: apply cards, chips, active-session/attention sections, richer summary surface.
- Modify `app/src/main/java/com/example/pawtrackr/ui/insights/InsightsScreen.kt`: apply cards, bars, semantic colors, and text-backed chart summaries.
- Create `app/src/test/java/com/example/pawtrackr/PawtrackrDesignTest.kt`: JVM tests for token values and semantic contracts.

## Task 1: Pawtrackr Design Token Tests

**Files:**
- Create: `app/src/test/java/com/example/pawtrackr/PawtrackrDesignTest.kt`
- Create later: `app/src/main/java/com/example/pawtrackr/ui/theme/PawtrackrDesign.kt`

- [ ] **Step 1: Write the failing token-contract tests**

```kotlin
package com.example.pawtrackr

import com.example.pawtrackr.ui.theme.PawtrackrRadius
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrSpacing
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PawtrackrDesignTest {
    @Test
    fun brandPrimaryMatchesIosDesignSystem() {
        assertEquals(0xFF6366F1.toInt(), PawtrackrStaticColor.BrandPrimaryArgb)
    }

    @Test
    fun spacingScaleMatchesIosDesignSystem() {
        assertEquals(4, PawtrackrSpacing.Xxs)
        assertEquals(6, PawtrackrSpacing.Xs)
        assertEquals(8, PawtrackrSpacing.Sm)
        assertEquals(12, PawtrackrSpacing.Md)
        assertEquals(16, PawtrackrSpacing.Lg)
        assertEquals(20, PawtrackrSpacing.Xl)
        assertEquals(24, PawtrackrSpacing.Xxl)
    }

    @Test
    fun radiiKeepCardsTighterThanPills() {
        assertEquals(10, PawtrackrRadius.Sm)
        assertEquals(14, PawtrackrRadius.Md)
        assertEquals(18, PawtrackrRadius.Lg)
        assertTrue(PawtrackrRadius.Pill > PawtrackrRadius.Lg)
    }

    @Test
    fun semanticColorsUseDistinctChannels() {
        val semanticColors = setOf(
            PawtrackrSemanticColor.SuccessArgb,
            PawtrackrSemanticColor.WarningArgb,
            PawtrackrSemanticColor.DangerArgb,
            PawtrackrSemanticColor.InfoArgb
        )

        assertEquals(4, semanticColors.size)
        assertEquals(0xFFEF4444.toInt(), PawtrackrSemanticColor.DangerArgb)
        assertEquals(0xFF10B981.toInt(), PawtrackrSemanticColor.SuccessArgb)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.example.pawtrackr.PawtrackrDesignTest --console=plain`

Expected: FAIL because `PawtrackrDesign.kt` and its token objects do not exist yet.

- [ ] **Step 3: Implement the design-token file**

Create `PawtrackrDesign.kt` with integer token contracts and Compose-facing `Color`, `Dp`, and `Shape` helpers.

- [ ] **Step 4: Run token tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests com.example.pawtrackr.PawtrackrDesignTest --console=plain`

Expected: PASS.

## Task 2: Shared Compose Components

**Files:**
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrCard.kt`
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrChip.kt`
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrSearchField.kt`
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrFab.kt`
- Create: `app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrMetrics.kt`

- [ ] **Step 1: Add component primitives**

Implement reusable wrappers that express the iOS-derived card, chip, search, FAB, KPI, section title, and bar-list styling through Material 3 primitives.

- [ ] **Step 2: Build to verify component APIs compile**

Run: `./gradlew assembleDebug --console=plain`

Expected: SUCCESS.

## Task 3: Theme Fallback Palette

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/theme/Theme.kt`

- [ ] **Step 1: Align non-dynamic light/dark fallback colors**

Replace the stock purple fallback palette with the Pawtrackr brand primary and semantic-friendly surface colors, while preserving Android 12+ dynamic color behavior.

- [ ] **Step 2: Run unit tests**

Run: `./gradlew testDebugUnitTest --console=plain`

Expected: PASS.

## Task 4: Dashboard Visual Parity Slice

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/dashboard/DashboardScreen.kt`

- [ ] **Step 1: Replace local card/KPI styling with shared components**

Use `PawtrackrCard`, `PawtrackrKpiCard`, `PawtrackrChip`, and section helpers. Keep all existing state fields and string resources.

- [ ] **Step 2: Build Dashboard changes**

Run: `./gradlew assembleDebug --console=plain`

Expected: SUCCESS.

## Task 5: Insights Visual Parity Slice

**Files:**
- Modify: `app/src/main/java/com/example/pawtrackr/ui/insights/InsightsScreen.kt`

- [ ] **Step 1: Replace local stat/bar components with shared components**

Use `PawtrackrKpiCard`, `PawtrackrBarList`, semantic colors, and card rails. Keep the existing ViewModel and chart data.

- [ ] **Step 2: Run full verification**

Run: `./gradlew testDebugUnitTest assembleDebug --console=plain`

Expected: SUCCESS with all existing tests plus `PawtrackrDesignTest`.

## Task 6: Commit First UI Batch

**Files:**
- Stage only the plan, tests, theme, components, Dashboard, and Insights files.

- [ ] **Step 1: Inspect status**

Run: `git status --short`

Expected: modified/new files from this plan plus the pre-existing untracked `.idea/inspectionProfiles/`.

- [ ] **Step 2: Commit**

Run:

```bash
git add docs/superpowers/plans/2026-06-29-ui-system-dashboard-insights.md \
  app/src/test/java/com/example/pawtrackr/PawtrackrDesignTest.kt \
  app/src/main/java/com/example/pawtrackr/ui/theme/PawtrackrDesign.kt \
  app/src/main/java/com/example/pawtrackr/ui/theme/Theme.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrCard.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrChip.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrSearchField.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrFab.kt \
  app/src/main/java/com/example/pawtrackr/ui/components/PawtrackrMetrics.kt \
  app/src/main/java/com/example/pawtrackr/ui/dashboard/DashboardScreen.kt \
  app/src/main/java/com/example/pawtrackr/ui/insights/InsightsScreen.kt
git commit -m "feat: add Pawtrackr UI foundation"
```

Expected: commit succeeds and `.idea/inspectionProfiles/` remains untracked.
