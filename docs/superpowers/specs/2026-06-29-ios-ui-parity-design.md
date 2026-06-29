# Pawtrackr Android iOS UI Parity Design

## Context

Pawtrackr Android is already a working Room-first Jetpack Compose port with onboarding, dashboard, clients and pets, checkout, insights, settings, Material You color, and client messaging. The iOS/macOS app at `/Users/mac/Desktop/Pawtrackr` has a richer visual system that Android has not fully matched yet.

The first improvement phase will focus on UI parity and visible product polish. Advanced engine layers such as embeddings, transaction ring buffers, thermal workload policy, and guided coachmarks will stay out of this phase unless needed to support the UI.

## Source Of Truth

Use the iOS files below as visual and interaction references:

- `Pawtrackr/UI/Theme/DesignSystem.swift`
- `Pawtrackr/UI/Theme/Animations.swift`
- `Pawtrackr/UI/Components/Card.swift`
- `Pawtrackr/UI/Components/Chip.swift`
- `Pawtrackr/UI/Components/SearchField.swift`
- `Pawtrackr/UI/Components/FAB.swift`
- `Pawtrackr/UI/Components/TimelineItem.swift`
- `Pawtrackr/UI/Components/PhotoWell.swift`
- `Pawtrackr/App/ContentView.swift`
- `Pawtrackr/App/MainTabView.swift`
- `Pawtrackr/Features/Dashboard/DashboardView.swift`
- `Pawtrackr/Features/Clients/ClientsView.swift`
- `Pawtrackr/Features/Clients/ClientCard.swift`
- `Pawtrackr/Features/Clients/ClientDetailView.swift`
- `Pawtrackr/Features/Clients/PetCard.swift`
- `Pawtrackr/Features/Clients/PetDetailView.swift`
- `Pawtrackr/Features/Checkout/CheckoutView.swift`
- `Pawtrackr/Features/Insights/InsightsView.swift`
- `Pawtrackr/Features/Settings/SettingsView.swift`

## Goals

1. Make Android visually feel like Pawtrackr, not a generic Material sample.
2. Preserve Android-native behavior, accessibility, touch targets, dynamic color, and lifecycle-safe Compose state.
3. Centralize styling in reusable Compose components before restyling screens.
4. Improve Dashboard, Clients, Checkout, Insights, Settings, and Onboarding without changing core data semantics.
5. Keep the work incremental and buildable after each screen pass.

## Non-Goals

- Do not implement embeddings, AppSearch, ring buffers, thermal policy, PowerSync, biometrics, Bluetooth printing, FCM, or full walkthrough coachmarks in this phase.
- Do not rename the current app package or migrate to Hilt in this phase.
- Do not rewrite repositories, Room schemas, checkout math, or seeded data unless a UI bug exposes a real data contract problem.
- Do not attempt pixel-perfect Apple UI that violates Android navigation, accessibility, or system gesture conventions.

## Design System

Create a Compose design system that maps the iOS tokens into Android:

- Brand primary: iOS `#6366F1` indigo as the fallback brand accent.
- Semantic colors: success `#10B981`, warning `#F59E0B`, danger `#EF4444`, info `#3B82F6`.
- Spacing: `4.dp`, `6.dp`, `8.dp`, `12.dp`, `16.dp`, `20.dp`, `24.dp`.
- Radii: `10.dp`, `14.dp`, `18.dp`, and pill shapes.
- Accent rails: `4.dp` leading rails for safety, active session, and attention states.
- Hairline borders: thin low-alpha outlines on cards and chips.
- Motion: subtle press-scale and spring-like transitions, disabled or reduced when system reduced motion is enabled.

Material You dynamic color should remain enabled on Android 12+, but app components should still use Pawtrackr semantic roles for safety, success, warning, and data status. This gives the app Android-native personalization without losing Pawtrackr's product identity.

## Core Components

Add reusable stateless Compose components before screen rewrites:

- `PawtrackrCard`: rounded surface, optional border, optional leading/top accent rail, clickable press-scale variant.
- `PawtrackrChip`: filled, outline, tinted, prominent, and semantic chip variants.
- `PawtrackrSearchField`: capsule search field with search icon, optional clear action, and focus treatment.
- `PawtrackrFab`: circular/extended primary action with loading and optional badge support.
- `PawtrackrTimelineItem`: status timeline row with node, connector, title, subtitle, and trailing value.
- `PhotoWell`: before/after media picker surface with placeholder, thumbnail, loading, and removable states.

These components should live in a focused UI package such as `ui/components` and be used by feature screens rather than duplicating styling locally.

## Screen Designs

### Dashboard

Redesign Dashboard around the iOS structure:

- Smart summary header with date/status context.
- KPI grid using `PawtrackrCard`.
- Quick actions for new client, clients, checkout/session work, and insights.
- Active sessions and needs-attention sections with accent rails.
- Recent activity rendered as timeline items.
- Revenue card with clear total and compact bar visualization.

The current dashboard data can remain the source. New actions may navigate only where Android already supports the destination.

### Clients

Clients is the highest-impact screen and the highest-risk file because `ClientsScreen.kt` is large.

Redesign it in place first, then split components if needed:

- Capsule search at the top.
- Horizontal filter chips and compact sort menu.
- Adaptive list/grid: compact phone list, expanded split-pane with richer cards.
- Client card with avatar initials, contact, pet count, pet-name preview, status chip, and leading accent rail.
- Full-width safety banner for aggressive-pet clients, matching iOS priority.
- Client detail with profile header, action chips, stat cards, pet cards, and visit timeline.
- Pet detail with behavior chips, safety warning, lifetime metrics, photo thumbnails, and checkout/check-in primary action.

Finish localization for Clients while touching the file, because it is the remaining unlocalized screen.

### Checkout

Move checkout toward the iOS stepped flow while keeping the current repository and math:

- Services step.
- Details/photos step.
- Payment/tip step.
- Review/confirm step.

The first implementation can still use `ModalBottomSheet`, but the internal structure should become step-based and less dense. Total, tip, and custom amount must remain `BigDecimal`-safe and must continue to use the existing `CheckoutViewModel`.

### Insights

Restyle Insights to use the iOS visual hierarchy:

- KPI strip.
- Revenue card with stronger typography and compact chart.
- Top services/categories as bar-list cards.
- Top clients/retention surface.
- Empty and loading states that match the shared design system.

Do not add new analytics math in this phase.

### Settings And Onboarding

Settings and Onboarding should adopt the same cards, spacing, and section structure:

- Business profile card.
- Save state and validation messages with semantic colors.
- Onboarding welcome with brand identity, not just a large emoji.
- Form fields aligned with the shared search/input visual rhythm.

## Data Flow

Feature ViewModels stay as the state owners. UI components remain stateless or locally stateful only for temporary presentation state such as expanded menus, dialog fields, and current checkout step.

Existing repositories and Room flows remain the single source of truth. No screen should read Room directly. No screen should introduce ad hoc business logic that belongs in domain models or ViewModels.

## Error And Empty States

Every redesigned screen must preserve clear loading, empty, and error behavior:

- Loading: centered progress or lightweight skeleton where the iOS pattern already uses skeletons.
- Empty: useful message plus a clear next action when safe.
- Error: visible message using `MaterialTheme.colorScheme.error` or Pawtrackr danger semantics.
- Disabled actions: remain visible, explainable through state text where needed, and keep minimum touch targets.

## Accessibility

The UI parity pass must keep Android accessibility first:

- Minimum touch target is `48.dp`.
- Do not rely on color alone for safety, active session, warning, or success states.
- Safety states use icon, text, and color together.
- Charts and visual summaries include nearby textual values.
- Animations respect reduced motion.
- Search, sort, filters, dialogs, and checkout steps remain keyboard and screen-reader reachable.

## Testing And Verification

Use the smallest test mix that proves the redesign did not break behavior:

- JVM tests: keep existing domain and checkout tests green.
- Build verification: `./gradlew testDebugUnitTest assembleDebug --console=plain`.
- Emulator smoke: launch, Dashboard, Clients list/detail, Message sheet, Checkout sheet, Insights, Settings.
- UI-tree checks: verify important labels/actions exist after restyling, especially Clients and Checkout.
- Screenshot or visual inspection: compare Android screens against iOS structure where available.

## Implementation Order

1. Add design tokens and reusable components.
2. Restyle Dashboard with shared components.
3. Restyle Clients and finish Clients string resources.
4. Restyle Checkout into a stepped sheet flow.
5. Restyle Insights.
6. Restyle Settings and Onboarding.
7. Run emulator smoke and final verification.

This order maximizes visible progress while keeping each phase buildable and reviewable.
