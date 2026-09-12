# Test Coverage and Metrics

This document tracks test coverage, performance baselines, and release validation results.

## Coverage by module

| Module | Unit Tests | Integration | Notes |
| --- | --- | --- | --- |
| `core/GlobalRDPLevel` | ✅ | Manual | Clamping, stage transitions, pressure scaling |
| `util/PlayerPressureSource` | ✅ | Manual | Region boundaries, coordinate handling |
| `util/PressureRegistry` | ✅ | Manual | Source aggregation, error handling |
| `region/RDPRegion` | ✅ | Manual | Pressure accumulation, clamping |
| `anomaly/Anomaly` | ✅ | Manual | Aging, intensity decay, lifecycle |
| `world/RDPWorldState` | ⚠️ Partial | Manual | NBT serialization not unit-tested |
| `RDPSimulationEngine` | ⚠️ Limited | Required | Full engine loop requires Minecraft |
| `RDPIntegrationManager` | ⚠️ Limited | Required | Mod detection requires loaded mods |

## Performance baselines

Recorded under Minecraft 1.12.2, Forge 14.23.5.2859, Unimined 1.4.1.

### Build times

| Command | Time | Notes |
| --- | --- | --- |
| `gradlew.bat clean test` | ~26s | Unit tests only; no Minecraft compile |
| `gradlew.bat build` | ~23s | Incremental (after test); includes remap |
| `gradlew.bat clean build` | ~50s | Full compile + remap |

### Pressure calculation latency (target load: 10 players, 1024 active regions)

| Metric | Target | Observed | Status |
| --- | --- | --- | --- |
| Per-region player enumeration | < 0.1ms | N/A | Depends on player list size |
| Per-tick pressure collection | < 10ms | N/A | Requires Minecraft environment |
| Total simulation per tick | < 50ms | N/A | Includes mutation, event processing |

## Release validation checklist

Before releasing, perform these steps:

- [ ] `gradlew.bat clean build` passes with no warnings
- [ ] Run 5-minute test play session with 0 players; verify no anomalies
- [ ] Run 5-minute test with 5 players spread across regions; verify pressure changes
- [ ] Run 30-minute endurance test with expected player load
- [ ] Record `Telemetry.pressureCalcDurationMs` max/min/avg via `/rdp` command
- [ ] Verify 95th percentile pressure calc < 10 ms
- [ ] Verify no memory leaks (check heap every 10 minutes)
- [ ] Document results below

## Release history

### v1.0.0 (2026-09-12)

**Build environment**: Windows 11, JDK 25, Gradle 9.6.0

**Test results**:
- Unit tests: ✅ PASS (5 test files, 17 assertions)
- Build: ✅ PASS (no compile warnings after `-Xlint:-options`)
- Integration: ⚠️ MANUAL PENDING

**Performance**:
- `gradlew.bat clean test`: 26s
- `gradlew.bat build`: 23s

**Notes**:
- Pressure source tests are deterministic and pass
- Region boundary tests (half-open interval) verified
- Negative coordinate handling confirmed
- Integration tests (player detection, dimension filtering) still pending

## Continuous integration

When setting up CI/CD pipeline:

```yaml
test:
  script:
    - gradlew.bat clean test
  artifacts:
    - build/reports/tests/

build:
  script:
    - gradlew.bat build
  artifacts:
    - build/libs/rdpcore-*.jar
    - build/reports/
```

Recommended: Run on every commit; use test results to gate PR merge.
