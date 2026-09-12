# Testing and scaling

## Quick start

Run tests and build:

```bat
# Run unit tests only (deterministic, ~26 seconds)
gradlew.bat clean test

# Full build with Minecraft compilation (deterministic + integration, ~23 seconds)
gradlew.bat build

# Both together
gradlew.bat clean build
```

## Test suite structure

Unit tests run without a Minecraft server. Integration testing is manual and requires a test environment.

### Unit tests (`src/test/java`)

**GlobalRDPLevelTest**
- RDP level clamping (negative → 0, > 1 → 1)
- Stage transitions at correct thresholds
- Pressure multiplier monotonicity across stages

**PlayerPressureSourceTest**
- Linear scaling: `pressure = playersInRegion × pressurePerPlayer`
- Half-open region boundaries (no double-counting at borders)
- Floor-based classification for negative coordinates
- Boundary cases: `x = regionMax - 1` (inside), `x = regionMax` (outside)

**PressureRegistryTest**
- Multiple sources aggregate correctly
- Null world/context handling

**RDPRegionTest**
- Region coordinate getters
- Local RDP level clamping
- Pressure clamping and accumulation
- Reality anchor count clamping

**AnomalyTest**
- Age increments each tick
- Intensity decays over time
- Anomaly becomes inactive after maxAge
- Intensity never goes negative

### Integration testing (manual)

Test these on a dedicated Forge server or single-player world:

1. **Player presence detection**: Place players in different regions and verify pressure readings via `/rdp pressure <x> <z>`
2. **Pressure scaling with load**: Add/remove players and confirm pressure changes match linear model
3. **Dimension filtering**: Verify pressure is 0 for players in other dimensions
4. **Stage progression**: Let world progress through RDP stages and verify anomaly spawning at thresholds
5. **Telemetry**: Record `Telemetry.pressureCalcDurationMs` under your target player load

## Scaling model

### Regional pressure

Player contribution is linear:

```
pressure = playersInRegion × pressurePerPlayer
```

Default registration: `new PlayerPressureSource(1.0D)` (but `RDPSimulationEngine` uses `0.05D`).

Simulation applies stage multiplier to all pressure sources:

| Stage | Multiplier | Effect |
| --- | ---: | --- |
| RDP-0 | 0.5 | Dampened; rare anomalies |
| RDP-I | 0.8 | Emerging distortions |
| RDP-II | 1.0 | Baseline; biome mutations |
| RDP-III | 1.2 | Regional instability |
| RDP-IV | 1.4 | Temporal effects |
| RDP-V | 1.6 | Dimensional leakage |
| RDP-VI | 1.8 | Spatial collapse |
| RDP-VII | 2.0 | Cosmological instability |
| RDP-X | 3.0 | Reality breakdown (end-game) |

### Performance

Player enumeration is `O(P)` per processed region, where `P` = online players.

**Budget per tick**: 50 ms (Forge default)

**Active region cache**: `ACTIVE_REGION_CACHE_SIZE` (default 1024)
- Limits regions processed per tick
- LRU eviction: most recently touched regions stay active

**Configuration alignment**: When changing player capacity, update:
- `ACTIVE_REGION_CACHE_SIZE` (regions)
- `SIMULATION_INTERVAL_TICKS` (how often to run simulation; default 200 = 10 seconds)
- `MUTATION_NORMAL_BUDGET` and `MUTATION_CRITICAL_BUDGET`

**Release validation**:
1. Record `Telemetry.pressureCalcDurationMs` across a 10-minute play session with target player load
2. Confirm 95th percentile < 10 ms and max < 20 ms
3. Verify total simulation duration (pressure + mutation + event) stays < 50 ms/tick

## Test execution workflow

### For developers

1. Make code changes
2. `gradlew.bat clean test` — ensures all unit tests pass
3. Test locally in a Forge environment
4. Commit changes with test results in message

### For release validation

1. `gradlew.bat clean build` — full compile, remap, and test
2. Deploy JAR to test modpack with expected player load
3. Run 1-2 hour play session; monitor `Telemetry.pressureCalcDurationMs` via `/rdp` command
4. Verify no unintended anomalies, mutations, or pressure spikes
5. Document results in release notes

## Troubleshooting

### Tests fail on compile

- Confirm JDK 8+ is installed: `java -version`
- Gradle cache corruption: `gradlew.bat clean`

### Tests pass but gameplay broken

- Pressure source registration order matters; later sources override earlier ones if not additive
- Check `/rdp debug` output for pressure calculation warnings
- Verify modpack has required dependencies (SRP, SCP, etc.)

### Pressure scaling doesn't match predictions

- Confirm stage multipliers are not being overridden in `RDPConfig`
- Check `Telemetry.pressureCalcDurationMs` — excessive time may indicate missed optimizations
- Run `/rdp pressure <x> <z>` at known player locations to verify region boundaries
