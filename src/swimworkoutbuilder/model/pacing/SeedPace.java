package swimworkoutbuilder.model.pacing;

import swimworkoutbuilder.model.units.Distance;
import swimworkoutbuilder.model.units.TimeSpan;

import java.util.Objects;

/**
 * Baseline pace for a given distance and time.
 * Stores the original distance/time and derives canonical speed (m/s).
 *
 * Minimal by design to align with current policy and callers.
 */
public final class SeedPace {
    private final Distance originalDistance;  // e.g., 100y or 100m
    private final TimeSpan time;              // time for that distance
    private final double speedMps;            // meters/second (0 if time==0)

    /**
     * Create a seed pace from a measured distance and time.
     * @param originalDistance distance swum (e.g., 100y or 100m)
     * @param time time to complete that distance
     */
    public SeedPace(Distance originalDistance, TimeSpan time) {
        this.originalDistance = Objects.requireNonNull(originalDistance, "originalDistance");
        this.time             = Objects.requireNonNull(time, "time");
        long ms = this.time.toMillis();
        this.speedMps = (ms <= 0L) ? 0.0 : (this.originalDistance.toMeters() / (ms / 1000.0));
    }

    /** The original baseline distance (e.g., 100y or 100m). */
    public Distance getOriginalDistance() { return originalDistance; }

    /** The measured time for the original distance. */
    public TimeSpan getTime() { return time; }

    /** Canonical speed in meters/second (0 if time==0). */
    public double speedMps() { return speedMps; }

    @Override
    public String toString() {
        return "SeedPace{" +
                "originalDistance=" + originalDistance +
                ", time=" + time +
                ", speedMps=" + String.format("%.3f", speedMps) +
                '}';
    }
}