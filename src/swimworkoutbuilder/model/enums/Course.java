package swimworkoutbuilder.model.enums;

/**
 * Represents the type of swimming course (i.e., pool length)
 * SCY = Short Course Yards (25 yards)
 * SCM = Short Course Meters (25 meters)
 * LCM = Long Course Meters (50 meters)
 */
public enum Course {
    SCY("Short Course Yards", 25, CourseUnit.YARDS),
    SCM("Short Course Meters", 25, CourseUnit.METERS),
    LCM("Long Course Meters", 50, CourseUnit.METERS);

    private final String description;
    private final int length;
    private final CourseUnit unit;

    Course(String description, int length, CourseUnit unit) {
        this.description = description;
        this.length = length;
        this.unit = unit;
    }

    public String getDescription() { return description; }
    public int getLength() { return length; }
    public CourseUnit getUnit() { return unit; }

    @Override
    public String toString() {
        return description + " (" + length + " " + unit + ")";
    }
}
