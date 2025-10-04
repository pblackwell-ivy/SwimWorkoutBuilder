package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.Equipment;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.units.Distance;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a single training set (e.g., "8 × 50 FREESTYLE @ ENDURANCE").
 *
 * Responsibilities:
 *  • Stores the essential attributes of one set:
 *      - stroke, reps, distance per rep (exact, via Distance value object),
 *      - effort level, optional notes, course context (SCY/SCM/LCM),
 *      - optional equipment modifiers.
 *  • Validates and normalizes distance so it is always a legal multiple of the pool length.
 *      - If an invalid distance is given (e.g., 75 in a 50m pool), it is snapped
 *        to the nearest valid multiple using ROUND UP.
 *  • Keeps the model consistent for pace calculations; pacing is computed elsewhere.
 *
 * Design notes:
 *  • Distances are stored with exact internal units (0.0001 m) in the Distance value object.
 *  • Course is required; distance normalization depends on it.
 *  • Equipment is modeled as an EnumSet so multiple training aids can be applied together.
 */
public class SwimSet {

    private StrokeType stroke;
    private int reps;
    private Distance distancePerRep; // exact (canonical stored inside Distance)
    private Effort effort;
    private String notes;            // optional
    private Course course;           // pool context for this set

    // Equipment used for this set (optional; defaults to none)
    private Set<Equipment> equipment = EnumSet.noneOf(Equipment.class);

    // --- Constructors ---

    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course,
                   String notes) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        if (distancePerRep == null || distancePerRep.rawUm4() <= 0)
            throw new IllegalArgumentException("distancePerRep must be > 0");
        if (course == null) throw new IllegalArgumentException("course must not be null");

        this.stroke = stroke;
        this.reps = reps;
        this.effort = effort;
        this.notes = (notes == null ? "" : notes);
        this.course = course;

        // Snap distance to a legal multiple of the pool length (ROUND UP).
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, course);
    }

    // Convenience (no notes)
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course) {
        this(stroke, reps, distancePerRep, effort, course, "");
    }

    // --- Getters / Setters ---

    public StrokeType getStroke() { return stroke; }
    public void setStroke(StrokeType stroke) { this.stroke = stroke; }

    public int getReps() { return reps; }
    public void setReps(int reps) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        this.reps = reps;
    }

    public Distance getDistancePerRep() { return distancePerRep; }
    public void setDistancePerRep(Distance distancePerRep) {
        if (distancePerRep == null || distancePerRep.rawUm4() <= 0)
            throw new IllegalArgumentException("distancePerRep must be > 0");
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, this.course);
    }

    public Effort getEffort() { return effort; }
    public void setEffort(Effort effort) { this.effort = effort; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = (notes == null ? "" : notes); }

    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        if (course == null) throw new IllegalArgumentException("course must not be null");
        this.course = course;
        // Re-snap existing distance to the new course
        this.distancePerRep = snapUpToCourseMultiple(this.distancePerRep, this.course);
    }

    public Set<Equipment> getEquipment() { return equipment; }
    public void setEquipment(Set<Equipment> equipment) {
        this.equipment = (equipment == null) ? EnumSet.noneOf(Equipment.class) : EnumSet.copyOf(equipment);
    }
    public void addEquipment(Equipment e) {
        if (e == null) return;
        if (equipment == null) equipment = EnumSet.noneOf(Equipment.class);
        equipment.add(e);
    }
    public void removeEquipment(Equipment e) {
        if (e == null || equipment == null) return;
        equipment.remove(e);
    }
    public boolean hasEquipment(Equipment e) {
        return equipment != null && equipment.contains(e);
    }

    @Override
    public String toString() {
        return "SwimSet{" +
                "stroke=" + stroke +
                ", reps=" + reps +
                ", distancePerRep=" + distancePerRep +
                ", effort=" + effort +
                ", course=" + course +
                (equipment != null && !equipment.isEmpty() ? ", equipment=" + equipment : "") +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }

    // --- Helpers ---

    /**
     * Snap a distance UP to the nearest legal multiple of the pool length for the given course.
     * Exact integer math on canonical units (0.0001 m), no floating-point drift.
     */
    private static Distance snapUpToCourseMultiple(Distance distance, Course course) {
        Distance poolLen = course.getLength();
        long d = distance.rawUm4();
        long p = poolLen.rawUm4();
        if (p <= 0) return distance; // safety

        long multiples = d / p;
        if (d % p != 0) multiples++; // round UP
        long snapped = Math.max(p, Math.multiplyExact(multiples, p)); // at least one pool length

        // Preserve the user's preferred display unit for the set distance
        return Distance.ofCanonicalUm4(snapped, distance.displayUnit());
    }
}