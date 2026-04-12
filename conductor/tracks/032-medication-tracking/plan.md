# Track 032: Medication Tracking

## Objective
Implement a comprehensive medication tracking system for Heart-Sense. This includes defining medications and schedules on the mobile app, providing quick-logging capabilities directly from the Wear OS watch (via app or Tile), delivering active reminders for missed doses, and analyzing correlations between medication intake (or missed doses) and physiological stress spikes.

## Key Files & Context
- **Data Layer**:
    - `mobile/src/main/java/com/heart/sense/data/db/Medication.kt` (New Entity: config & schedule)
    - `mobile/src/main/java/com/heart/sense/data/db/MedicationIntake.kt` (New Entity: actual logged events)
    - `mobile/src/main/java/com/heart/sense/data/db/MedicationDao.kt` (New)
    - `mobile/src/main/java/com/heart/sense/data/MedicationRepository.kt` (New)
- **Wear OS Sync**:
    - `mobile/src/main/java/com/heart/sense/data/WearableCommunicationRepository.kt` (Update to sync active meds to watch)
    - `wear/src/main/java/com/heart/sense/wear/data/WearableCommunicationRepository.kt` (Update to receive meds, sync logs to phone)
- **Wear OS UI**:
    - `wear/src/main/java/com/heart/sense/wear/ui/MedicationTileService.kt` (New: Quick-log tile)
- **Reminders**:
    - `mobile/src/main/java/com/heart/sense/service/MedicationReminderWorker.kt` (New: WorkManager for scheduling reminders)
- **Analytics & UI**:
    - `mobile/src/main/java/com/heart/sense/ui/DashboardScreen.kt` (Overlay meds on charts)
    - `mobile/src/main/java/com/heart/sense/data/AlertHandler.kt` (Annotate alerts if preceded by a missed dose)

## Implementation Steps

### Phase 1: Database & Sync Architecture
1. **Entities & DAOs**: Create `Medication` (id, name, dose, frequency, reminderTime) and `MedicationIntake` (id, medId, timestamp, source [Phone/Watch]).
2. **Repository**: Implement `MedicationRepository` to handle CRUD operations.
3. **Data Layer Sync**: Use Google Play Services Wearable DataClient to sync the list of active medications from the phone to the watch, and sync logged intakes from the watch back to the phone.

### Phase 2: Mobile Configuration & Reminders
1. **Config UI**: Add a "Medications" screen to the mobile app where users can add/edit prescriptions and set daily reminder times.
2. **Reminder Engine**: Utilize `WorkManager` (or `AlarmManager` for exact alarms) to schedule local notifications reminding the user to take their medication. Ensure the notification has a "Log as Taken" action button.

### Phase 3: Wear OS Quick-Log
1. **Wear UI**: Build a simple list UI on the watch showing active medications.
2. **Wear Tile**: Create a `MedicationTileService` allowing the user to swipe to a tile and tap a pill icon to instantly log an intake.

### Phase 4: Clinical Correlation & Analytics
1. **Chart Overlay**: Update the `DashboardScreen` (Vico Charts) to render vertical markers or icons at the exact timestamp a medication was logged.
2. **Missed Dose Analytics**: When `AlertHandler` processes a high-stress event, query the `MedicationRepository`. If a scheduled medication was missed within the last X hours, append a note to the Alert context (e.g., "Potential trigger: Missed ADHD Meds").

## Verification & Testing
- **Unit Tests**: Implement `MedicationRepositoryTest` to verify schedule parsing and missed-dose detection logic.
- **Sync Validation**: Ensure adding a medication on the phone instantly updates the Wear OS Tile.
- **Reminder Validation**: Confirm that reminders fire accurately, even when the phone is dozing.
- **Data Integrity**: Verify that quick-logs from the watch accurately reach the phone's Room database and appear on the dashboard.

## Migration & Rollback
- **Database Migration**: Ensure Room database version is incremented gracefully with a proper Migration object from the previous version.
