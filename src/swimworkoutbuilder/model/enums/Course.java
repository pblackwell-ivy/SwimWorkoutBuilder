package swimworkoutbuilder.model.enums;

/**
 * Represents the type of swimming course (i.e., pool length)
 * SCY = Short Course Yards (25 yards)
 * SCM = Short Course Meters (25 meters)
 * LCM = Long Course Meters (50 meters)
 *
 * v2: added performance multiplier used by pace calculation.
 * SC pools have more turns, which means more push-offs and more underwaters as a percentage
 * of the distance compared to an LC pool.
 * Typical defaults:
 *  - SCY: 1.00 (25y: more turns, more underwater as a percentage of distance)
 *  - SCM: 1.04 (25m: underwater as a percentage of distance is slightly less than SCY)
 *  - LCM: 1.07 (50m: half as many turns as SCY/SCM, less underwater vs. swimming on surface)
 */
public enum Course {
    SCY("Short Course Yards", 25, swimworkoutbuilder.model.enums.CourseUnit.YARDS, 1.00),
    SCM("Short Course Meters", 25, swimworkoutbuilder.model.enums.CourseUnit.METERS, 1.04),
    LCM("Long Course Meters", 50, swimworkoutbuilder.model.enums.CourseUnit.METERS, 1.07);

    private final String description;
    private final int length;
    private final CourseUnit unit;
    private final double multiplier; // <-- NEW in v2

    Course(String description, int length, CourseUnit unit, double multiplier) {
        this.description = description;
        this.length = length;
        this.unit = unit;
        this.multiplier = multiplier;
    }

    public String getDescription() { return description; }
    public int getLength() { return length; }
    public CourseUnit getUnit() { return unit; }
    /** Performance multiplier applied in pace calculations. */
    public double multiplier() { return multiplier; }

    @Override
    public String toString() {
        return description + " (" + length + " " + unit + ")" + " (" + multiplier + ")";
    }
}