Here is the Kotlin-based architectural plan for your Pixel 9 and Pixel Watch 3.

Step 1: The Kotlin APIs You Will Use
You will be building a project with two modules: a wear app and an app (mobile).

Health Services API (WearOS): This is the holy grail for your project. It bypasses raw sensor polling and gives you processed, battery-optimized health data.

Wearable Data Layer API: This is how your Pixel Watch 3 will talk to your Pixel 9 (using MessageClient for instant alerts and DataClient for syncing history).

Kotlin Coroutines & Flows: Used to handle asynchronous sensor data streams seamlessly.

Step 2: The Two-Tiered Monitoring System
Your brilliant idea of a "watching closer" mode maps perfectly to two distinct clients within the Health Services API:

Mode 1: The "Batch Processing" Mode (PassiveMonitoringClient)
This is your everyday, battery-saving mode.

How it works: You register a PassiveListenerConfig for Heart Rate and Activity State. The WearOS system batches this data in the background and delivers it to your app periodically without waking up the main processor constantly.

The Trigger: You evaluate the batched data. If you see USER_ACTIVITY_STATE_PASSIVE (sitting still) combined with a high average HR in the batch, you trigger the alert and transition to Mode 2.

Mode 2: The "Watching Closer" Mode (MeasureClient)
When an anomaly is detected, your app shifts gears to get real-time data.

How it works: You start a Foreground Service on the watch (which requires showing an ongoing notification to the user, e.g., "Monitoring High HR..."). Inside this service, you use the MeasureClient.

The Data: MeasureClient provides a continuous, per-second stream of Heart Rate (DataType.HEART_RATE_BPM).

The Resolution: If the heart rate drops back to normal for a set duration (e.g., 2 minutes), you stop the Foreground Service, stop the MeasureClient, and fall back to Mode 1 (PassiveMonitoringClient).

Step 3: Required Android Permissions
Because health data is highly sensitive, your AndroidManifest.xml will need specific permissions to make this work:

android.permission.BODY_SENSORS (To read heart rate)

android.permission.BODY_SENSORS_BACKGROUND (To read it while the app is closed)

android.permission.ACTIVITY_RECOGNITION (To know if you are stationary or moving)

android.permission.FOREGROUND_SERVICE_HEALTH (To run the "Watching Closer" mode in the background)

android.permission.POST_NOTIFICATIONS (To alert you)

Step 4: Communication with the Pixel 9
When the Watch detects the anomaly, it needs to wake up the phone.

Using the Wearable.getMessageClient(context), the watch sends a tiny, instant payload (e.g., path = "/hr_alert", data = "125bpm_stationary").

A WearableListenerService running on your Pixel 9 intercepts this message.

The Pixel 9 then triggers a high-priority, full-screen intent or a loud notification so you don't miss it.