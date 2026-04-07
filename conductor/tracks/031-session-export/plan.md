# Implementation Plan: Track 031 - Session-Based Clinical Export

## Overview
This track shifts the data architecture from "User-Centric" to "Visit-Centric," ensuring that clinical data can be exported and attached to patient records without storing PII in the app.

## Sub-tasks
1. **Database Refactor:**
    - Add `visitId: String?` field to `OvernightMeasurement`, `Alert`, and `Intervention` entities.
    - Create a `Session` entity to track session start/end times and metadata.
2. **Session Management:**
    - Implement `SessionRepository` to handle the generation of UUIDs and active session state.
    - Add "Start Visit" and "Stop Visit" controls to the `SettingsViewModel`.
3. **Data Tagging:**
    - Update `AlertHandler` and `OvernightDataRepository` to automatically attach the current `activeVisitId` to all new records.
4. **EHR-Ready Export:**
    - Develop a specialized export tool that bundles all data for a specific `VisitID` into a single, encrypted JSON packet.
    - Include clinical metadata (e.g., Device ID, Calibration Status) in the export.
