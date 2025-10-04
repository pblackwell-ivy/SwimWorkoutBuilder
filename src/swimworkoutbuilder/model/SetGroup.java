package swimworkoutbuilder.model;

import swimworkoutbuilder.model.units.Distance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A logical group of sets (e.g., Warmup, Drills, Main, Cooldown).
 * Holds its sets, optional notes, optional rest after the group, and display order.
 *
 * MVP: container only — no timing math here. Distance helpers are provided for convenience.
 */
public class SetGroup {
    private final UUID id;
    private String name;            // label, e.g., "Warmup"
    private int reps;               // how many times this whole group repeats (>=1)
    private int order;              // display / sequence order in the workout
    private final List<SwimSet> sets = new ArrayList<>();
    private int restAfterGroupSec;  // optional rest after the group
    private String notes;           // optional

    // Constructors
    public SetGroup(String name, int reps, int order) {
        if (reps < 1) throw new IllegalArgumentException("Group reps must be >= 1");
        this.id = UUID.randomUUID();
        this.name = name;
        this.reps = reps;
        this.order = order;
    }

    // Getters & Setters
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

    public List<SwimSet> getSets() { return sets; }

    public int getRestAfterGroupSec() { return restAfterGroupSec; }
    public void setRestAfterGroupSec(int restAfterGroupSec) { this.restAfterGroupSec = Math.max(0, restAfterGroupSec); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Mutators
    public void addSet(SwimSet set) {
        if (set != null) sets.add(set);
    }

    public void removeSet(SwimSet set) {
        sets.remove(set);
    }

    public int getSetCount() {
        return sets.size();
    }

    // ---- Distance helpers (Distance-aware) ----

    /** Sum of all sets' distance for a single pass of the group (exact Distance). */
    public Distance singlePassDistance() {
        long totalUm4 = 0L; // sum in canonical 0.0001 m units
        for (SwimSet s : sets) {
            long perRepUm4 = s.getDistancePerRep().rawUm4();
            totalUm4 = Math.addExact(totalUm4, Math.multiplyExact(perRepUm4, s.getReps()));
        }
        // Totals are generally displayed in meters; choose METERS as display unit.
        return Distance.ofCanonicalUm4(totalUm4, Distance.Unit.METERS);
    }

    /** Total distance including group repeats (exact Distance). */
    public Distance totalDistance() {
        long singleUm4 = singlePassDistance().rawUm4();
        long totalUm4  = Math.multiplyExact(singleUm4, Math.max(1, (long) reps));
        return Distance.ofCanonicalUm4(totalUm4, Distance.Unit.METERS);
    }

    // ---- Legacy meter helpers (kept for compatibility) ----

    /** Sum of all sets' distance for a single pass of the group (meters, rounded). */
    @Deprecated
    public int singlePassDistanceMeters() {
        return (int) Math.round(singlePassDistance().toMeters());
    }

    /** Total distance including group repeats (meters, rounded). */
    @Deprecated
    public int totalDistanceMeters() {
        return (int) Math.round(totalDistance().toMeters());
    }

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