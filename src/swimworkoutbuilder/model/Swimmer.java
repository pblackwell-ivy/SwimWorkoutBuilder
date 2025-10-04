package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.pacing.SeedPace;   // <-- add this
import swimworkoutbuilder.model.units.Distance;    // only if you use the overloads
import swimworkoutbuilder.model.units.TimeSpan;    // only if you use the overloads

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a swimmer using the workout builder.
 * Stores identity and baseline seed paces (per stroke). Each seed is a {@link SeedPace}
 * that preserves the original entry (distance+unit and time) and caches canonical speed (m/s).
 *
 * Ergonomic overloads let you set seeds as "per-100y" or "per-100m" without extra boilerplate.
 */
public class Swimmer {
    private final UUID id;
    private String firstName;
    private String lastName;
    private String preferredName;   // optional / nullable
    private String teamName;        // optional / nullable
    private final Map<StrokeType, SeedPace> seedPaces = new EnumMap<>(StrokeType.class);

    // --- Constructors ---

    public Swimmer(String firstName, String lastName) {
        this(UUID.randomUUID(), firstName, lastName, null, null);
    }

    public Swimmer(String firstName, String lastName, String preferredName, String teamName) {
        this(UUID.randomUUID(), firstName, lastName, preferredName, teamName);
    }

    // UUID-aware constructor (e.g., repository load)
    public Swimmer(UUID id, String firstName, String lastName, String preferredName, String teamName) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.teamName = teamName;
    }

    // --- Identity ---

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPreferredName() { return preferredName; }
    public String getTeamName() { return teamName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    // --- Seeds (per stroke) ---

    /** Returns the baseline seed for the given stroke, or null if not set. */
    public SeedPace getSeedTime(StrokeType stroke) {
        return seedPaces.get(stroke);
    }

    /** Creates or replaces the baseline seed for the given stroke. */
    public void updateSeedTime(StrokeType stroke, SeedPace seed) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(seed,   "seed");
        seedPaces.put(stroke, seed);
    }

    /** Convenience: set a seed defined as "per 100 yards" in <b>seconds</b>. */
    public void updateSeed100Y(StrokeType stroke, double secondsPer100Y) {
        updateSeedTime(stroke,
                new SeedPace(Distance.ofYards(100), TimeSpan.ofSeconds(secondsPer100Y)));
    }

    /** Convenience: set a seed defined as "per 100 meters" in <b>seconds</b>. */
    public void updateSeed100M(StrokeType stroke, double secondsPer100M) {
        updateSeedTime(stroke,
                new SeedPace(Distance.ofMeters(100), TimeSpan.ofSeconds(secondsPer100M)));
    }

    /** Convenience: explicit distance + time overload (e.g., 200m in 120.0s). */
    public void updateSeedTime(StrokeType stroke, Distance seedDistance, TimeSpan seedTime) {
        Objects.requireNonNull(seedDistance, "seedDistance");
        Objects.requireNonNull(seedTime,     "seedTime");
        updateSeedTime(stroke, new SeedPace(seedDistance, seedTime));
    }

    public boolean hasSeed(StrokeType stroke) { return seedPaces.containsKey(stroke); }

    public void clearSeed(StrokeType stroke) { seedPaces.remove(stroke); }

    /** Removes all seeds for this swimmer. */
    public void clearAllSeeds() { seedPaces.clear(); }

    // --- Derived helpers (nice to have) ---

    /** Returns canonical speed (m/s) for a stroke, or NaN if no seed set. */
    public double speedMps(StrokeType stroke) {
        SeedPace s = seedPaces.get(stroke);
        return (s == null) ? Double.NaN : s.speedMps();
    }

    /** Returns true if all four primary strokes have seeds set. */
    public boolean hasCoreSeeds() {
        return hasSeed(StrokeType.FREESTYLE)
                && hasSeed(StrokeType.BACKSTROKE)
                && hasSeed(StrokeType.BREASTSTROKE)
                && hasSeed(StrokeType.BUTTERFLY);
    }

    @Override
    public String toString() {
        return "Swimmer(" +
                "id=" + id + ", " +
                "name=" + firstName + " " + lastName + ", " +
                "preferredName=" + (preferredName == null ? "" : preferredName) + ", " +
                "teamName=" + (teamName == null ? "" : teamName) + ", " +
                "seedPaces=" + seedPaces +
                ')';
    }
}