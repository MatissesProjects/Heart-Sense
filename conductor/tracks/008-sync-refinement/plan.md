# Plan: Sync Refinement & Wear UX

## Phase 1: Data Layer Debouncing
- [x] **Task 1.1:** Add a debounce mechanism to `SettingsRepository` (Mobile) to limit sync frequency during slider drags.
- [x] **Task 1.2:** Add a similar debounce mechanism to `SettingsRepository` (Wear).

## Phase 2: Wear UX & Rotary Input
- [x] **Task 2.1:** Update `MainActivity.kt` (Wear) to support rotary input for scrolling.
- [x] **Task 2.2:** Implement `onRotaryScrollEvent` for the threshold slider.

## Phase 3: Conflict Resolution
- [x] **Task 3.1:** Add `lastUpdated` timestamp to the `Settings` data model.
- [x] **Task 3.2:** Update `SettingsListenerService` (both modules) to only apply updates if the incoming timestamp is newer than the local one.
