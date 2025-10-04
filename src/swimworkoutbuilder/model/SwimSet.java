package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.Equipment;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.utils.Distance;

import java.util.EnumSet;
import java.util.Set;

import static swimworkoutbuilder.model.utils.Distance.MultipleRounding;
import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

/**
 * Represents a single training set (e.g., "8 × 50 FREESTYLE @ ENDURANCE").
 *
 * Responsibilities:
 *  • Stores the essential attributes of one set:
 *      - stroke, reps, distance per rep (canonical in meters),
 *      - effort level, optional notes, course context (SCY/SCM/LCM),
 *      - optional equipment modifiers.
 *  • Validates and normalizes distance so it is always a legal multiple of the pool length.
 *      - If an invalid distance is given (e.g., 75m in a 50m pool), it is automatically snapped
 *        to the nearest valid multiple (rounding policy defined in Distance utility).
 *  • Keeps the model consistent for pace calculations, avoiding invalid or ambiguous reps.
 *
 * Design notes:
 *  • Distances are stored in meters internally to keep calculations uniform regardless of pool unit.
 *  • The associated Course is always required; all distance normalization depends on it.
 *  • Equipment is modeled as an EnumSet so multiple training aids can be applied together.
 *  • This class is lightweight and contains no pacing logic — goal/interval/rest are calculated
 *    externally by PacePolicy implementations (e.g., DefaultPacePolicy).
 *
 * Example usage:
 *   SwimSet set = new SwimSet(StrokeType.FREESTYLE, 1,
 *                              Distance.yardsToMeters(50),
 *                              Effort.RACE_PACE, Course.SCY, "desc");
 *   set.addEquipment(Equipment.FINS);
 *   // toString -> SwimSet{stroke=FREESTYLE, reps=1, distancePerRepMeters=46, effort=RACE_PACE, course=SCY, equipment=[FINS]}
 */
public class SwimSet {

    private StrokeType stroke;
    private int reps;
    private int distancePerRepMeters; // canonical distance in meters (snapped to course multiple)
    private Effort effort;
    private String notes;             // optional
    private Course course;            // pool context for this set

    // Equipment used for this set (optional; defaults to none)
    private Set<Equipment> equipment = EnumSet.noneOf(Equipment.class);

    // --- Constructors ---

    public SwimSet(StrokeType stroke, int reps, int distancePerRepMeters, Effort effort, Course course, String notes) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        if (distancePerRepMeters <= 0) throw new IllegalArgumentException("distancePerRepMeters must be > 0");
        if (course == null) throw new IllegalArgumentException("course must not be null");

        this.stroke = stroke;
        this.reps = reps;
        this.effort = effort;
        this.notes = (notes == null ? "" : notes);
        this.course = course;

        // Snap distance to a legal multiple of the pool length (round UP to mimic common UX)
        this.distancePerRepMeters = Distance.toMultipleOfCourse(distancePerRepMeters, course, MultipleRounding.UP);
    }

    // Convenience (no notes)
    public SwimSet(StrokeType stroke, int reps, int distancePerRepMeters, Effort effort, Course course) {
        this(stroke, reps, distancePerRepMeters, effort, course, "");
    }

    // --- Getters / Setters ---

    public StrokeType getStroke() { return stroke; }
    public void setStroke(StrokeType stroke) { this.stroke = stroke; }

    public int getReps() { return reps; }
    public void setReps(int reps) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        this.reps = reps;
    }

    public int getDistancePerRepMeters() { return distancePerRepMeters; }
    public void setDistancePerRepMeters(int distancePerRepMeters) {
        if (distancePerRepMeters <= 0) throw new IllegalArgumentException("distancePerRepMeters must be > 0");
        this.distancePerRepMeters = Distance.toMultipleOfCourse(distancePerRepMeters, this.course, MultipleRounding.UP);
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
        this.distancePerRepMeters = Distance.toMultipleOfCourse(this.distancePerRepMeters, this.course, MultipleRounding.UP);
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
                ", distancePerRepMeters=" + distancePerRepMeters +
                ", effort=" + effort +
                ", course=" + course +
                (equipment != null && !equipment.isEmpty() ? ", equipment=" + equipment : "") +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}