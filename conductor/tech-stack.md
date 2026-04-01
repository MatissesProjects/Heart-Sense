# Heart-Sense: Tech Stack

## Mobile (Pixel 9)
- **Language:** Kotlin
- **Framework:** Jetpack Compose
- **Concurrency:** Coroutines & Flows
- **Communication:** Wearable Data Layer API (`MessageClient`, `DataClient`)

## WearOS (Pixel Watch 3)
- **Language:** Kotlin
- **Framework:** Jetpack Compose for Wear OS
- **API:** Health Services API (`PassiveMonitoringClient`, `MeasureClient`)
- **Services:** Foreground Services for real-time monitoring.

## Common
- **Build System:** Gradle (Kotlin DSL)
- **Dependency Injection:** Hilt (Recommended)
- **Architecture:** MVVM / Clean Architecture
