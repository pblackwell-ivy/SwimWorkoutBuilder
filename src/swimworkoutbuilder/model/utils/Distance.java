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
 *   int poolLen = Distance.poolLengthMeters(Course.SCY); // -> ~23 (rounded from 22.86)
 *   int legalDistance = Distance.toMultipleOfCourse(73, Course.LCM, Distance.MultipleRounding.UP); // -> 100
 *   double inSeedUnits = Distance.metersToSeedUnits(50, CourseUnit.YARDS); // -> 54.68 yd
 */
public final class Distance {
    private Distance() {}

    // --- Conversions ---

    /** Convert meters to the seed unit (yards or meters). */
    public static double metersToSeedUnits(double meters, CourseUnit seedUnit) {
        return (seedUnit == CourseUnit.YARDS) ? meters / 0.9144 : meters;
    }

    /** Scale an arbitrary distance (already in the seed unit) into 100s (e.g., 50 → 0.5). */
    public static double distance100s(double distanceInSeedUnits) {
        return distanceInSeedUnits / 100.0;
    }

    /** Convert yards to meters, rounding to the nearest whole meter for canonical storage. */
    public static int yardsToMeters(int yards) {
        return (int) Math.round(yardsToMetersExact(yards));
    }

    /** Exact yards→meters conversion (no rounding). Useful for precise snapping math. */
    public static double yardsToMetersExact(double yards) {
        return yards * 0.9144;
    }

    // --- Pool lengths ---

    /**
     * Exact pool length in meters for a given course.
     * SCY uses 25 * 0.9144 = 22.86 m (double), SCM=25.0 m, LCM=50.0 m.
     */
    public static double poolLengthMetersExact(Course course) {
        if (course == null) throw new IllegalArgumentException("course must not be null");
        return (course.getUnit() == CourseUnit.YARDS)
                ? course.getLength() * 0.9144         // 25y -> 22.86 m
                : (double) course.getLength();         // 25m or 50m
    }

    /**
     * Integer pool length in meters for legacy call sites.
     * Returns Math.round(poolLengthMetersExact(course)).
     */
    public static int poolLengthMeters(Course course) {
        return (int) Math.round(poolLengthMetersExact(course));
    }

    // --- Distance normalization ---

    public enum MultipleRounding { NEAREST, UP, DOWN }

    /**
     * Snap an arbitrary distance (meters) to a legal multiple of the pool length.
     * Uses exact (double) pool length to avoid SCY drift (e.g., 400 yd ↔ 365.76 m).
     *
     * @param meters distance to snap (meters, integer canonical)
     * @param course course context (SCY/SCM/LCM)
     * @param mode   rounding policy (UP, DOWN, NEAREST)
     * @return snapped distance in meters (integer canonical)
     */
    public static int toMultipleOfCourse(int meters, Course course, MultipleRounding mode) {
        double L = poolLengthMetersExact(course);
        if (L <= 0) throw new IllegalArgumentException("Invalid pool length for " + course);
        if (meters <= 0) return (int) Math.round(L);

        double q = Math.floor(meters / L);
        double base = q * L;
        double r = meters - base;

        double snapped;
        switch (mode) {
            case DOWN:    snapped = base; break;
            case UP:      snapped = (q + 1.0) * L; break;
            case NEAREST:
            default:      snapped = (r < L - r) ? base : (q + 1.0) * L; break;
        }
        return (int) Math.round(snapped);
    }
}