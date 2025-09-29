package swimworkoutbuilder.model;

import swimworkoutbuilder.model.enums.CourseUnit;
import java.util.Date;

/**
 * Represents a swimmer’s baseline pace in seconds for 100 units (yards or meters).
 * Used as the foundation for estimating times, intervals, and workout projections.
 * This class does not know which stroke it belongs to — stroke association is
 * managed at the Swimmer level.
 *
 * Responsibilities:
 * - Store the time (in seconds) for 100 units
 * - Track whether the units are yards or meters
 * - Record the last updated timestamp for this pace
 */
public class SeedPace100 {
    private double seedTimeSec;     // time for 100 (in seconds)
    private CourseUnit unit;        // YARDS or METERS
    private Date lastUpdated;       // timestamp of last update

    // Constructors
    public SeedPace100(double seedTimeSec, CourseUnit unit) {
        this.seedTimeSec = seedTimeSec;     // the time to swim 100 units (yards or meters), expressed in seconds
        this.unit = unit;                   // the unit of measure for the distance (YARDS or METERS)
        this.lastUpdated = new Date();      // the timestamp of the most recent update to this seedpace
    }

    public SeedPace100() {
        this(0.0, CourseUnit.YARDS);        // default fallback
    }

    // Getters & Setters

    public double getSeedTimeSec() {
        return seedTimeSec;
    }

    public void setSeedTimeSec(double seedTimeSec) {
        this.seedTimeSec = seedTimeSec;
        this.lastUpdated = new Date();   // <-- add this line
    }

    public CourseUnit getCourseUnit() {
        return unit;
    }

    public void setCourseUnit(CourseUnit unit) {
        this.unit = unit;
        this.lastUpdated = new Date();
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime()) ;
    }

    @Override
    public String toString() {
        return "SeedPace100{" + " seedTimeSec=" + seedTimeSec + ", unit=" + unit + ", lastUpdated=" + lastUpdated + '}';
    }
}
