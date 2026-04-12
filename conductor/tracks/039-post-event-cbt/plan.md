# Track 039: Post-Event CBT

## Objective
Implement a mobile-side Cognitive Behavioral Therapy (CBT) reflection flow triggered 30 minutes after a detected stress event, allowing the user to capture subjective context while it is still fresh.

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/ui/CbtReflectionScreen.kt` (New UI)
- `mobile/src/main/java/com/heart/sense/data/db/CbtJournalEntry.kt` (New Entity)
- `mobile/src/main/java/com/heart/sense/service/CbtTriggerWorker.kt` (New: Delayed notification)

## Implementation Steps
1. **Delayed Trigger**: Use `WorkManager` to schedule a notification exactly 30 minutes after a stress alert is dismissed or resolved.
2. **Subjective Journaling**: Build a simple UI with multiple-choice and text fields (e.g., "What was your emotion?", "What were you thinking?", "Rate your stress 1-10").
3. **Data Linkage**: Link the `CbtJournalEntry` to the specific `AlertId` to correlate subjective feelings with physiological data.
4. **Longitudinal Analysis**: Show a "Feelings vs. Physiology" trend on the dashboard.
