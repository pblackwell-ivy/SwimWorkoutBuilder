package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.pacing.PacePolicy;
import swimworkoutbuilder.model.utils.Distance;

/**
 * A single training set (e.g., 8 x 50 FREE @ ENDURANCE).
 * Distance per rep is stored in METERS internally; convert at I/O boundaries.
 */
public class SwimSet {

    private StrokeType stroke;
    private int reps;
    private int distancePerRepMeters; // canonical distance in meters
    private Effort effort;
    private String notes;             // optional

    // --- Constructors ---
    public SwimSet(StrokeType stroke, int reps, int distancePerRepMeters, Effort effort, String notes) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        if (distancePerRepMeters <= 0) throw new IllegalArgumentException("distancePerRepMeters must be > 0");
        this.stroke = stroke;
        this.reps = reps;
        this.distancePerRepMeters = distancePerRepMeters;
        this.effort = effort;
        this.notes = (notes == null ? "" : notes);
    }

    // --- Getters / Setters ---
    public StrokeType getStroke() { return stroke; }
    public void setStroke(StrokeType stroke) { this.stroke = stroke; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getDistancePerRepMeters() { return distancePerRepMeters; }
    public void setDistancePerRepMeters(int distancePerRepMeters) { this.distancePerRepMeters = distancePerRepMeters; }

    public Effort getEffort() { return effort; }
    public void setEffort(Effort effort) { this.effort = effort; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "SwimSet{" +
                "stroke=" + stroke +
                ", reps=" + reps +
                ", distancePerRepMeters=" + distancePerRepMeters +
                ", effort=" + effort +
                (notes != null ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}