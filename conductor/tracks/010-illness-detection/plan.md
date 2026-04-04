# Plan: Illness Detection

## Phase 1: Database Infrastructure
- [ ] **Task 1.1:** Add Room dependencies to `libs.versions.toml` and `build.gradle.kts` (Wear).
- [ ] **Task 1.2:** Create `OvernightMeasurement` entity and DAO.
- [ ] **Task 1.3:** Implement `HeartSenseDatabase` and provide via Hilt.

## Phase 2: Detection Logic
- [ ] **Task 2.1:** Create `OvernightDataRepository` to handle data aggregation.
- [ ] **Task 2.2:** Update `PassiveMonitoringService` to store data and detect sleep transitions.
- [ ] **Task 2.3:** Implement `IllnessEvaluator` based on the designed risk weights.

## Phase 3: Communication & UI
- [ ] **Task 3.1:** Add `PATH_ILLNESS_ALERT` to `Constants.kt`.
- [ ] **Task 3.2:** Update `WearableCommunicationRepository` (Wear) to send detailed illness data.
- [ ] **Task 3.3:** Update `AlertListenerService` (Mobile) to display rich illness alerts.
