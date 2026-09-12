# Performance tuning guide

This guide helps optimize RDP Core for your modpack and player load.

## Diagnosis

First, establish your baseline metrics:

1. **Player load**: How many concurrent players?
2. **Active regions**: How many regions are typically loaded?
3. **Target tick budget**: How much time per tick can RDP use? (usually 50 ms)

Launch your server and collect data:

```
/rdp debug
```

This shows:
- Current global RDP level
- Active regions cached
- Pressure calculation time (ms)
- Recent anomaly spawns

## Tuning parameters

Edit `config/rdpcore.cfg` to adjust:

### Regional load

```ini
region_size_chunks=16
active_region_cache_size=1024
```

- Smaller `region_size_chunks` → more regions, higher overhead
- Larger `active_region_cache_size` → keeps more regions in memory but smoother load distribution

**Recommendation**: Use default (16, 1024) unless profiling shows bottleneck.

### Simulation frequency

```ini
# How often to run the full simulation (ticks)
# 200 ticks = 10 seconds at 20 ticks/second
simulation_interval_ticks=200
```

Higher = less frequent updates, lower CPU cost, but regions update slower.

### Pressure decay

```ini
region_pressure_decay=0.001
```

Higher = pressure drops faster, fewer anomalies. Tuning this scales difficulty without rebalancing thresholds.

### Mutation budgets

```ini
mutation_normal_budget=500
mutation_critical_budget=2000
```

Higher budget = more mutations per tick, more chunks rewritten.
Lower budget = better performance but slower world changes.

Adjust based on `/rdp debug` output (look for "Mutations processed").

## Profiling

### On server console

Run `/rdp debug` and note `Pressure calc(ms)`. If consistently > 10 ms:

1. Check player count (`/list`)
2. Reduce `active_region_cache_size` by 25%
3. Increase `simulation_interval_ticks` by 50%
4. Re-test after 5 minutes

### Using JFR (Java Flight Recorder)

For deeper profiling (advanced):

```bash
java -XX:+FlightRecorder -XX:StartFlightRecording=delay=30s,duration=60s,filename=rdp-profile.jfr -jar forge-server.jar
```

Analyze with JDK Mission Control or IntelliJ Profiler.

## Common scenarios

### Low-end server (5-10 players, 2 GB RAM)

```ini
active_region_cache_size=256
simulation_interval_ticks=400
region_pressure_decay=0.002
mutation_normal_budget=250
mutation_critical_budget=1000
```

### Mid-range server (10-30 players, 8 GB RAM)

```ini
active_region_cache_size=1024
simulation_interval_ticks=200
region_pressure_decay=0.001
mutation_normal_budget=500
mutation_critical_budget=2000
```

### High-end server (50+ players, 16+ GB RAM)

```ini
active_region_cache_size=2048
simulation_interval_ticks=100
region_pressure_decay=0.0005
mutation_normal_budget=1000
mutation_critical_budget=4000
```

## Monitoring

Add these to your server monitoring dashboard:

- `Telemetry.pressureCalcDurationMs` (should be < 10ms for normal load)
- Active region count (should match player spread)
- Global RDP level (should progress smoothly without jumps)
- Anomaly count (should spike at stage transitions)

If pressure calc time spikes:

1. Check for player teleports (players moving between distant regions)
2. Look for anomaly spawning cascade (multiple high-intensity anomalies)
3. Verify dimension filtering is working (check logs for "world != world" messages)

## Troubleshooting

### "Pressure calc(ms)" is high (> 20 ms)

- Reduce `active_region_cache_size`
- Increase `simulation_interval_ticks`
- Check if any mod is adding many pressure sources (look at logs on server start)

### Anomalies spawn too frequently

- Increase `region_pressure_decay` (pressure drops faster)
- Reduce stage multipliers in `RDPConfig` (code change required)
- Lower player count nearby (spread players out)

### Anomalies never spawn

- Check global RDP level (`/rdp debug`)
- Confirm thresholds haven't been raised (check config)
- Verify regions have players (anomalies need pressure)

### Memory usage grows over time

- Reduce `active_region_cache_size`
- Reduce `max_anomalies_active_per_world`
- Increase `simulation_interval_ticks` (fewer regions processed per tick)
