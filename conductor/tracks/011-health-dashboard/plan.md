# Plan: Health Dashboard

## Phase 1: Infrastructure
- [x] **Task 1.1:** Add Vico dependencies to `libs.versions.toml` and `mobile/build.gradle.kts`.
- [x] **Task 1.2:** Create `DailyAverage` data model in `mobile`.
- [x] **Task 1.3:** Implement `DailyAverageRepository` to manage historical data.

## Phase 2: UI Implementation
- [x] **Task 2.1:** Create `HealthDashboard.kt` with line charts for HR and RR.
- [x] **Task 2.2:** Add `DailyAverageRow` for the summary table.
- [x] **Task 2.3:** Integrate the dashboard into `SettingsScreen.kt`.

## Phase 3: Data Visualization
- [x] **Task 3.1:** Highlight days with alert triggers in the UI.
- [x] **Task 3.2:** Implement baseline markers on the charts.
