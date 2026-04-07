# Track 026: Voice-to-Tag

## Overview
Enable hands-free tagging of behavioral events using voice.

## Objectives
- Integrate with Google Assistant (App Actions).
- Allow users/caregivers to say "Tag sensory overload" or "Tag transition".
- Map voice commands to specific tags in the `AlertsRepository`.
- Provide haptic confirmation on the watch when a tag is applied via voice.

## Key Files
- `mobile/src/main/res/xml/shortcuts.xml` (New)
- `mobile/src/main/java/com/heart/sense/receiver/VoiceCommandReceiver.kt` (New)
