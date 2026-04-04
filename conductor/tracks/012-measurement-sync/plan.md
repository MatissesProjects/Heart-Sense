# Plan: Measurement Sync

## Phase 1: Communication Pattern
- [x] **Task 1.1:** Add `PATH_REQUEST_SYNC` and `PATH_SYNC_DATA` to `Constants.kt`. (Done: PATH_REQUEST_SYNC and PATH_SYNC_BATCH added)
- [x] **Task 1.2:** Update `WearableCommunicationRepository` (Mobile) to request sync from watch.
- [x] **Task 1.3:** Implement `SyncRequestReceiver` (Wear) to fetch data from Room and send back in batches. (Implemented in SettingsListenerService and OvernightDataRepository)

## Phase 2: Serialization & Batching
- [x] **Task 2.1:** Design a lightweight pipe-delimited schema for measurement batches.
- [x] **Task 2.2:** Implement batching logic to send 50 samples per message.

## Phase 3: Phone-side Storage
- [x] **Task 3.1:** Update `DailyAverageRepository` to receive and store raw batches.
- [x] **Task 3.2:** Implement daily aggregation logic on the phone.
