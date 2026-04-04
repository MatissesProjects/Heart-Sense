# Plan: Pipeline Optimization & Refactoring

## Phase 1: Logic Consolidation
- [x] **Task 1.1:** Refactor `Settings.kt` (Wear) to include `effectiveThreshold` logic.
- [x] **Task 1.2:** Create `HeartRateEvaluator.kt` to unify monitoring actions.
- [x] **Task 1.3:** Update `PassiveMonitoringService` and `HealthMonitoringService` to use the new evaluator.

## Phase 2: Service Coordination
- [x] **Task 2.1:** Add `isActiveMonitoringRunning` flag to `SettingsDataStore`.
- [x] **Task 2.2:** Update `PassiveMonitoringService` to skip alerts when HMS is active.

## Phase 3: Communication Efficiency
- [x] **Task 3.1:** Implement `CapabilityClient` in `WearableCommunicationRepository` to filter for the phone node.


## Phase 4: Boot Resilience
- [x] **Task 4.1:** Implement `BootReceiver` and register for `BOOT_COMPLETED` to resume monitoring on startup.
