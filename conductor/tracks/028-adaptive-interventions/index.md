# Track 028: RL Interventions

## Overview
Use Reinforcement Learning to personalize calming interventions.

## Objectives
- Track which interventions (e.g., biofeedback, sensory break) the user selects.
- Measure the "Recovery Rate" (how fast HR returns to baseline) after each intervention.
- Use a simple on-device RL model to recommend the most effective intervention for the current context.
- Continuously adapt the recommendation engine based on user outcomes.

## Key Files
- `mobile/src/main/java/com/heart/sense/ai/RecoveryLearner.kt` (New)
- `mobile/src/main/java/com/heart/sense/data/InterventionRepository.kt` (New)
