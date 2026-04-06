# Track 022: Predictive Stress AI (Precursor Detection)

## Overview
This track implements on-device machine learning to predict stress behaviors (meltdowns, agitation) up to 10 minutes before they occur by identifying physiological precursors.

## Goals
-   [ ] Implement a `StressPredictor` using TensorFlow Lite.
-   [ ] Use multi-modal inputs (HR, HRV, Motion, Noise) to forecast stress scores.
-   [ ] Create a "14-day Personalization" phase to calibrate the AI for the specific user.
-   [ ] Trigger "Pre-Alerts" (10-minute warnings) for caregivers to implement de-escalation.

## Next Steps
1.  Gather sample multi-modal data sets for training a prototype model.
2.  Implement the `TFLiteRegressor` on-device.
3.  Add "Prediction Confidence" to the caregiver notifications.
