package swimworkoutbuilder.model.utils;

import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.CourseUnit;

/**
 * Utility class for handling distance conversions and normalization in SwimWorkoutBuilder.
 *
 * Responsibilities:
 *  • Provide conversions between meters and yards (e.g., for seed times vs. workout storage).
 *  • Represent distances in consistent internal units (all canonical distances are stored in meters).
 *  • Calculate "per-100" scaling factors for goal time calculations.
 *  • Determine pool length in meters for a given course (SCY/SCM/LCM).
 *  • Normalize arbitrary distances to valid multiples of the pool length.
 *      - Ensures sets like "75m in a 50m pool" snap to 100m (or nearest multiple).
 *      - Rounding policy (UP, DOWN, NEAREST) can be chosen to match app behavior.
 *
 * Design notes:
 *  • This class is final and has a private constructor — it is a pure static utility.
 *  • It separates unit conversions and normalization logic from SwimSet/Workout classes,
 *    keeping the model clean and avoiding duplicated math.
 *
 * Example usage:
 *   int poolLen = Distance.poolLengthMeters(Course.SCY); // -> 25 * 0.9144 = 23m approx
 *   int legalDistance = Distance.toMultipleOfCourse(73, Course.LCM, Distance.MultipleRounding.UP); // -> 100
 *   double inSeedUnits = Distance.metersToSeedUnits(50, CourseUnit.YARDS); // -> 54.68 yd
 */
public final class Distance {
    private Distance() {}

    // --- Existing utilities ---
    public static double metersToSeedUnits(double meters, CourseUnit seedUnit) {
        return (seedUnit == CourseUnit.YARDS) ? meters / 0.9144 : meters;
    }

    public static double distance100s(double distanceInSeedUnits) {
        return distanceInSeedUnits / 100.0;
    }

    public static int yardsToMeters(int yards) {
        return (int) Math.round(yards * 0.9144);
    }

    // --- New helpers for pool-aware snapping ---

    /** Pool length in meters for a given course (SCY/SCM/LCM). */
    public static int poolLengthMeters(Course course) {
        if (course == null) throw new IllegalArgumentException("course must not be null");
        return (course.getUnit() == CourseUnit.YARDS)
                ? yardsToMeters(course.getLength())
                : course.getLength();
    }

    public enum MultipleRounding { NEAREST, UP, DOWN }

    /**
     * Snap an arbitrary distance (meters) to a legal multiple of the pool length.
     * Use UP to mimic apps that round 25 → 50 in a 50m pool, etc.
     */
    public static int toMultipleOfCourse(int meters, Course course, MultipleRounding mode) {
        int L = poolLengthMeters(course);
        if (L <= 0) throw new IllegalArgumentException("Invalid pool length for " + course);
        if (meters <= 0) return L;

        int q = meters / L;
        int r = meters % L;
        if (r == 0) return meters;

        switch (mode) {
            case DOWN:    return q * L;
            case UP:      return (q + 1) * L;
            case NEAREST:
            default:      return (r < L - r) ? q * L : (q + 1) * L;
        }
    }
}