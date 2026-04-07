# Track 031: Session-Based Clinical Export (EHR Ready)

## Overview
Implement an anonymized "Visit Mode" that tags all data to a specific clinical session ID rather than a persistent user identity.

## Objectives
- Implement a **"Start Session"** and **"End Session"** flow in the mobile app.
- Generate a unique, UUID-based **`VisitID`** for each session.
- Ensure all alerts, interventions, and measurements recorded during the session are tagged with the `VisitID`.
- Create a specialized **JSON/HL7-lite export** format for easy ingestion into Electronic Health Record (EHR) systems like Epic or Cerner.
- Remove persistent user linking to comply with strictly anonymized clinical protocols.

## Key Files
- `mobile/src/main/java/com/heart/sense/data/SessionRepository.kt` (New)
- `mobile/src/main/java/com/heart/sense/data/db/OvernightMeasurement.kt` (Update)
- `mobile/src/main/java/com/heart/sense/data/Alert.kt` (Update)
- `mobile/src/main/java/com/heart/sense/ui/reports/SessionExportScreen.kt` (New)
