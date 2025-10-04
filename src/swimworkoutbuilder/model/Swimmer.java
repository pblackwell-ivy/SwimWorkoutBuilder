package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.StrokeType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a swimmer using the workout builder.
 * A swimmer has a unique ID, a first name, last name, and optional preferred name, and an optional team name, plus
 * a collection of baseline seed paces (per stroke.) Each seed pace is stored as a SeedPace100 and
 * represents the swimmer’s performance benchmark for 100 units (yards/meters).
 * <p><b>Responsibilities:</b>
 * <ul>
 *     <li>Store swimmer identity and seeds.</li>
 *     <li>Provide accessors and mutators for seed paces.</li>
 *     <li>Serve as the anchor for linking workouts to an individual.</li>
 * </ul>
 * <p><b>Design Notes:</b>
 * <ul>
 *     <li>The Swimmer class is immutable.</li>
 *     <li>The Swimmer class is designed to be used as a key and as a value in a Map.</li>
 * </ul>
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * Swimmer swimmer = new Swimmer("Parker", "Blackwell");
 * }</pre>
 * @author parkerblackwell
 * @version 1.0
 * @since 2025-10-03
 * @see SeedPace100
 */
public class Swimmer {
    private final UUID id;
    private String firstName;
    private String lastName;
    private String preferredName;   // optional / nullable
    private String teamName;        // optional /
    private final Map<StrokeType, SeedPace100> seedPaces = new EnumMap<>(StrokeType.class);

    // Bare minimum constructor (UUID augo-generated)
    public Swimmer(String firstName, String lastName) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Normal constructor (UUID auto-generated)
    public Swimmer(String firstName, String lastName, String preferredName, String teamName) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.teamName = teamName;
    }

    // UUID-aware constructor used by SwimmerRepository when loading swimmers with id from CSV
    public Swimmer(UUID id, String firstName, String lastName, String preferredName, String teamName) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.teamName = teamName;
    }

    // Getters
    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPreferredName() { return preferredName; }
    public String getTeamName() { return teamName; }

    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    // Returns the baseline seed for the given stroke, or null if not set.
    public SeedPace100 getSeedTime(StrokeType stroke) {
        return seedPaces.get(stroke);
    }

    // Creates or replaces the baseline seed for the given stroke.  Callers construct SeedPace100 with seconds + unit
    public void updateSeedTime(StrokeType stroke, SeedPace100 seed) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(seed, "seed");
        seedPaces.put(stroke, seed);
    }

    public boolean hasSeed(StrokeType stroke) {
        return seedPaces.containsKey(stroke);
    }

    public void clearSeed(StrokeType stroke) {
        seedPaces.remove(stroke);
    }

    @Override
    public String toString() {
        return "Swimmer(" + "id=" + id + ", " +
                "name= " + firstName + " " + lastName + '\'' + ", " +
                "preferredName= " + preferredName + '\'' + ", " +
                "teamName= " + teamName + ", " +
                "seedPaces=" + seedPaces + '}';
    }
}
