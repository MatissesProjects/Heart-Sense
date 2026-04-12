# Track 036: Air Quality & Weather

## Objective
Correlate localized Air Quality Index (AQI), humidity, and barometric pressure drops with physiological arousal (Heart Rate, Respiratory Rate) and behavioral agitation.

## Key Files & Context
- `mobile/src/main/java/com/heart/sense/data/WeatherRepository.kt` (New: Weather/AQI API integration)
- `mobile/src/main/java/com/heart/sense/data/db/EnvironmentalContext.kt` (New Entity)
- `mobile/src/main/java/com/heart/sense/data/AlertHandler.kt`: Annotate alerts with weather data.

## Implementation Steps
1. **Weather API**: Integrate OpenWeatherMap or similar API to fetch local AQI, temperature, and pressure.
2. **Periodic Background Fetch**: Schedule a `Worker` to pull environmental data every hour.
3. **Stress Correlation**: Analyze if sudden barometric pressure drops or high AQI levels precede periods of elevated "Agitation Scores" (Pacing/Fidgeting).
4. **UI Insights**: Show an "Environmental Trigger" summary on the dashboard (e.g., "Agitation increases by 15% when humidity exceeds 80%").
