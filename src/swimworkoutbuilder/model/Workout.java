package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.units.Distance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A structured swim workout designed for a specific swimmer.
 * MVP: container + metadata. All timing math is handled by PacePolicy/WorkoutPrinter.
 */
public class Workout {

    // Identity & metadata
    private final UUID id;
    private UUID swimmerId;                 // the swimmer this workout is for
    private String name;                    // e.g., "Tuesday Threshold"
    private Course course;                  // SCY, SCM, or LCM
    private String notes;                   // optional workout-level notes (theme, focus)

    // Defaults (used by printer between groups)
    private int defaultRestBetweenGroupsSeconds = 0;  // applied between consecutive groups if group override not set

    // Contents
    private final List<SetGroup> groups = new ArrayList<>();

    // --- Constructors ---

    public Workout(UUID swimmerId, String name, Course course) {
        this.id = UUID.randomUUID();
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(name, "name");
        this.course = Objects.requireNonNull(course, "course");
    }

    public Workout(UUID swimmerId, String name, Course course, String notes, int defaultRestBetweenGroupsSeconds) {
        this.id = UUID.randomUUID();
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(name, "name");
        this.course = Objects.requireNonNull(course, "course");
        this.notes = notes;
        this.defaultRestBetweenGroupsSeconds = Math.max(0, defaultRestBetweenGroupsSeconds);
    }

    // --- Basic getters/setters ---

    public UUID getId() { return id; }

    public UUID getSwimmerId() { return swimmerId; }
    public void setSwimmerId(UUID swimmerId) { this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId"); }

    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name, "name"); }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = Objects.requireNonNull(course, "course"); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getDefaultRestBetweenGroupsSeconds() { return defaultRestBetweenGroupsSeconds; }
    public void setDefaultRestBetweenGroupsSeconds(int seconds) {
        this.defaultRestBetweenGroupsSeconds = Math.max(0, seconds);
    }

    // --- Groups management (ordered & mutable) ---

    public List<SetGroup> getGroups() { return groups; }

    public int getGroupCount() { return groups.size(); }

    public void addSetGroup(SetGroup group) {
        if (group != null) groups.add(group);
    }

    public void insertSetGroup(int index, SetGroup group) {
        if (group == null) return;
        groups.add(index, group); // will throw if index invalid (MVP: fail fast)
    }

    public SetGroup removeSetGroup(int index) {
        return groups.remove(index);
    }

    public void moveGroup(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return;
        SetGroup g = groups.remove(fromIndex); // throws if fromIndex invalid
        groups.add(toIndex, g);                // throws if toIndex invalid
    }

    public void swapGroups(int i, int j) {
        if (i == j) return;
        SetGroup a = groups.get(i); // throws if invalid
        SetGroup b = groups.get(j);
        groups.set(i, b);
        groups.set(j, a);
    }

    // --- Distance helpers ---

    /**
     * NEW: Sum of group distances for a single pass across all groups, as a Distance.
     * Uses existing group meter totals and wraps them as Distance for now.
     * (Once SetGroup is Distance-aware, this method can sum canonically without conversion.)
     */
    public Distance singlePassDistance() {
        return Distance.ofMeters(singlePassDistanceMeters());
    }

    /**
     * NEW: Total distance including group repeats, as a Distance.
     */
    public Distance totalDistance() {
        return Distance.ofMeters(totalDistanceMeters());
    }

    /** Sum of group distances for a single pass across all groups (meters). */
    @Deprecated
    public int singlePassDistanceMeters() {
        int sum = 0;
        for (SetGroup g : groups) {
            sum += g.singlePassDistanceMeters();
        }
        return sum;
    }

    /** Total distance including group repeats (meters). */
    @Deprecated
    public int totalDistanceMeters() {
        int sum = 0;
        for (SetGroup g : groups) {
            sum += g.totalDistanceMeters();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", swimmerId=" + swimmerId +
                ", name='" + name + '\'' +
                ", course=" + course +
                ", groups=" + groups.size() +
                ", defaultRestBetweenGroupsSeconds=" + defaultRestBetweenGroupsSeconds +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}