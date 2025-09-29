package swimworkoutbuilder.model;

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

    // Distance helpers (meters, since SwimSet stores meters)
    /** Sum of all sets' distance for a single pass of the group (meters). */
    public int singlePassDistanceMeters() {
        int sum = 0;
        for (SwimSet s : sets) {
            sum += s.getReps() * s.getDistancePerRepMeters();
        }
        return sum;
    }

    /** Total distance including group repeats (meters). */
    public int totalDistanceMeters() {
        return singlePassDistanceMeters() * Math.max(1, reps);
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