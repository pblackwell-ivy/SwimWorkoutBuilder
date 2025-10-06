package swimworkoutbuilder.model;

import swimworkoutbuilder.model.units.Distance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a **logical group of swim sets** within a {@link Workout}.
 *
 * <p>A {@code SetGroup} defines an ordered collection of {@link SwimSet}s
 * — for example, *Warmup*, *Drills*, *Main*, or *Cooldown* — each
 * with its own repeats, notes, and optional rest after completion.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Group related {@link SwimSet}s under a single label and order index.</li>
 *   <li>Track how many times this group repeats (e.g., “Main ×4”).</li>
 *   <li>Optionally specify rest time after completing the group.</li>
 *   <li>Provide helper methods to calculate total group distance
 *       using canonical {@link Distance} units.</li>
 *   <li>Ensure immutability of ID and validity of group repetition counts.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>The group does not perform timing or pacing logic — only structural data storage.</li>
 *   <li>Distances are computed canonically using {@link Distance} (internally stored in meters).</li>
 *   <li>Intended to be managed and displayed within a {@link Workout} tree or UI component.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * SetGroup warmup = new SetGroup("Warmup", 1, 1);
 * warmup.addSet(new SwimSet(StrokeType.FREESTYLE, 4, Distance.ofYards(50),
 *                           Effort.EASY, Course.SCY, "Smooth technique"));
 * warmup.setRestAfterGroupSec(60);
 *
 * System.out.println(warmup.totalDistance());
 * }</pre>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see SwimSet
 * @see Workout
 */
public class SetGroup {

    // ----------------------------------------------------------
    // Core fields
    // ----------------------------------------------------------

    private final UUID id;                // unique group identifier
    private String name;                  // label, e.g., "Warmup"
    private int reps;                     // how many times this group repeats (>=1)
    private int order;                    // display/sequence order in the workout
    private final List<SwimSet> sets = new ArrayList<>();
    private int restAfterGroupSec;        // optional rest after completing the group
    private String notes;                 // optional description or focus notes

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    /**
     * Creates a new {@code SetGroup}.
     *
     * @param name  display name for the group (e.g., "Main", "Cooldown")
     * @param reps  number of repetitions for the group (must be ≥1)
     * @param order order index used for display or sorting within a {@link Workout}
     * @throws IllegalArgumentException if {@code reps < 1}
     */
    public SetGroup(String name, int reps, int order) {
        if (reps < 1) throw new IllegalArgumentException("Group reps must be >= 1");
        this.id = UUID.randomUUID();
        this.name = name;
        this.reps = reps;
        this.order = order;
    }

    // ----------------------------------------------------------
    // Accessors / Mutators
    // ----------------------------------------------------------

    /** Returns the unique identifier for this group. */
    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getReps() { return reps; }
    public void setReps(int reps) {
        if (reps < 1) throw new IllegalArgumentException("Group reps must be >= 1");
        this.reps = reps;
    }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    /** Returns all {@link SwimSet}s contained in this group. */
    public List<SwimSet> getSets() { return sets; }

    public int getRestAfterGroupSec() { return restAfterGroupSec; }
    public void setRestAfterGroupSec(int restAfterGroupSec) {
        this.restAfterGroupSec = Math.max(0, restAfterGroupSec);
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ----------------------------------------------------------
    // Set management
    // ----------------------------------------------------------

    /** Adds a {@link SwimSet} to this group (ignores nulls). */
    public void addSet(SwimSet set) {
        if (set != null) sets.add(set);
    }

    /** Removes a specific {@link SwimSet} from this group (if present). */
    public void removeSet(SwimSet set) {
        sets.remove(set);
    }

    /** Returns how many {@link SwimSet}s this group currently contains. */
    public int getSetCount() {
        return sets.size();
    }

    // ----------------------------------------------------------
    // Distance helpers (canonical)
    // ----------------------------------------------------------

    /**
     * Returns the sum of all sets’ distances for one full pass through the group.
     *
     * <p>All math is done in canonical 0.0001 m precision, using the
     * internal representation of {@link Distance}. The returned value
     * is expressed in meters by default for display and analytics.</p>
     */
    public Distance singlePassDistance() {
        long totalMicroUnits = 0L;
        for (SwimSet s : sets) {
            long perRepMicroUnits = s.getDistancePerRep().rawMicroUnits();
            totalMicroUnits = Math.addExact(totalMicroUnits,
                    Math.multiplyExact(perRepMicroUnits, s.getReps()));
        }
        return Distance.ofCanonicalMicroUnits(totalMicroUnits, Distance.Unit.METERS);
    }

    /**
     * Returns the total distance of this group, including any group repetitions.
     * <p>Equivalent to {@code singlePassDistance() × reps}.</p>
     */
    public Distance totalDistance() {
        long singleMicroUnits = singlePassDistance().rawMicroUnits();
        long totalMicroUnits  = Math.multiplyExact(singleMicroUnits, Math.max(1, (long) reps));
        return Distance.ofCanonicalMicroUnits(totalMicroUnits, Distance.Unit.METERS);
    }

    // ----------------------------------------------------------
    // Legacy compatibility (meters only)
    // ----------------------------------------------------------

    /** Returns single-pass distance in meters (rounded). */
    @Deprecated
    public int singlePassDistanceMeters() {
        return (int) Math.round(singlePassDistance().toMeters());
    }

    /** Returns total distance (including repeats) in meters (rounded). */
    @Deprecated
    public int totalDistanceMeters() {
        return (int) Math.round(totalDistance().toMeters());
    }

    // ----------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------

    @Override
    public String toString() {
        return "SetGroup{" +
                "name='" + name + '\'' +
                ", reps=" + reps +
                ", order=" + order +
                ", sets=" + sets.size() +
                ", restAfterGroupSec=" + restAfterGroupSec +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}