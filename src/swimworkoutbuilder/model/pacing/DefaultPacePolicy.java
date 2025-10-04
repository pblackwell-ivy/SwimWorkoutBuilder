package swimworkoutbuilder.model.pacing;

import swimworkoutbuilder.model.SeedPace100;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.DistanceFactors;   // still used in goal calc
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.Equipment;
import swimworkoutbuilder.model.enums.StrokeType;

import java.util.Objects;
import java.util.Set;

/**
 * Multiplier-based MVP policy for computing goal, interval, and rest.
 * The seed pace per 100 (by stroke, in its own units) is the baseline.
 *
 * Effort-aware distance logic:
 *  - EASY / ENDURANCE: distance multiplier is skipped (flat pacing across distance).
 *  - THRESHOLD / RACE_PACE / VO2_MAX / SPRINT: apply DistanceFactors (longer reps drift slower).
 *
 * Rest policy (automatic scaling):
 *  - Rest is computed as a % of the goal time, based on effort and distance ratio r=(rep/100).
 *  - Intervals (goal + rest) are rounded to the nearest 5 seconds.
 *
 * Notes:
 * - Distances are stored internally in meters in SwimSet.
 * - We convert the repeat distance into the seed’s units (yards/meters) before dividing by 100.
 * - Course multiplier captures performance differences (turns/underwater) separate from unit conversion.
 * - A fatigue multiplier hook exists but is disabled for MVP.
 */
public class DefaultPacePolicy implements PacePolicy {

    private static final double YARD_TO_METER = 0.9144;
    private static final double METER_TO_YARD = 1.0 / YARD_TO_METER;

    // Flip to false to silence debug printing
    private static final boolean DEBUG = true;

    @Override
    public double goalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        Objects.requireNonNull(workout, "workout");
        Objects.requireNonNull(set, "set");
        Objects.requireNonNull(swimmer, "swimmer");

        // 1) Seed per-100 (seconds) and its units
        StrokeType stroke = set.getStroke();
        SeedPace100 seed = swimmer.getSeedTime(stroke);
        if (seed == null) {
            throw new IllegalStateException("Missing seed for stroke: " + stroke);
        }
        double seedPer100Sec = seed.getSeedTimeSec();
        CourseUnit seedUnit = seed.getCourseUnit();

        // 2) Convert repeat distance (stored in meters) into the seed's units
        double repDistanceInSeedUnits =
                (seedUnit == CourseUnit.YARDS)
                        ? set.getDistancePerRepMeters() * METER_TO_YARD
                        : set.getDistancePerRepMeters();

        // 3) Gather multipliers
        Effort effort = set.getEffort();
        double mEffort  = (effort == null) ? 1.0 : effort.paceMultiplier();
        double mDist    = usesDistanceFactor(effort)
                ? DistanceFactors.forDistance(set.getDistancePerRepMeters())
                : 1.0; // EASY/ENDURANCE => flat pacing across distance
        double mCourse  = workout.getCourse().multiplier();
        double mEquip   = equipmentProduct(set.getEquipment());

        // Future hook: fatigue multiplier (depends on repIndex, set order, etc.)
        double mFatigue = 1.0;

        // 4) Scale seed by distance/100 in seed units, then apply multipliers
        double distanceScale = repDistanceInSeedUnits / 100.0;
        double goal = seedPer100Sec * distanceScale * mEffort * mDist * mCourse * mEquip * mFatigue;

        if (DEBUG) {
            System.out.printf(
                    "[DEBUG] %s rep #%d calc: %.2fs × %.2f (effort) × %.2f (dist%s) × %.2f (course) × %.2f (equip) × %.2f (fatigue) × %.2f (rep/100) = %.2fs%n",
                    stroke, repIndex + 1,
                    seedPer100Sec,
                    mEffort,
                    mDist, usesDistanceFactor(effort) ? "" : " skipped",
                    mCourse,
                    mEquip,
                    mFatigue,
                    distanceScale,
                    goal
            );
        }

        return goal;
    }

    // ---------- NEW: interval uses rounded-to-5s and rest scales with effort × distance ----------

    @Override
    public int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int goalRounded = (int) Math.round(goalSeconds(workout, set, swimmer, repIndex));

        // rep distance in SEED units → r = (rep/100)
        SeedPace100 seed = swimmer.getSeedTime(set.getStroke());
        if (seed == null) throw new IllegalStateException("Missing seed for " + set.getStroke());
        double repDistanceInSeedUnits = (seed.getCourseUnit() == CourseUnit.YARDS)
                ? set.getDistancePerRepMeters() * METER_TO_YARD
                : set.getDistancePerRepMeters();
        double r = distanceRatio(repDistanceInSeedUnits);

        double pct = restPercent(set.getEffort(), r);
        int rest = (int) Math.round(goalRounded * pct);

        if (DEBUG) {
            System.out.printf("[DEBUG-REST] r=%.2f, effort=%s, rest%%=%.1f%% -> rest=%ds%n",
                    r, set.getEffort(), pct * 100.0, rest);
        }
        return Math.max(0, rest);
    }

    @Override
    public int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int goalRounded = (int) Math.round(goalSeconds(workout, set, swimmer, repIndex));
        int rest = restSeconds(workout, set, swimmer, repIndex);
        int interval = goalRounded + rest;
        int rounded = roundToNearest5(interval);
        if (DEBUG && rounded != interval) {
            System.out.printf("[DEBUG-INT] interval %ds -> rounded to %ds%n", interval, rounded);
        }
        return rounded;
    }

    @Override
    public String timingLabel(Workout workout, SwimSet set) {
        // Preserve legacy label behavior
        int rest = restSeconds(workout, set, null, 0); // swimmer not used in this path; safe here
        return "rest :" + rest;
    }

    // --- helpers ---

    private static boolean usesDistanceFactor(Effort e) {
        if (e == null) return false;
        switch (e) {
            case THRESHOLD:
            case RACE_PACE:
            case VO2_MAX:
            case SPRINT:
                return true;
            case EASY:
            case ENDURANCE:
            default:
                return false;
        }
    }

    private static double equipmentProduct(Set<Equipment> equipment) {
        if (equipment == null || equipment.isEmpty()) return 1.0;
        double m = 1.0;
        for (Equipment eq : equipment) {
            if (eq != null) m *= eq.multiplier();
        }
        return m;
    }

    // ---- NEW helpers for rest scaling & interval rounding ----

    /** Distance ratio r = (rep distance in seed units) / 100, clamped to avoid degenerate values. */
    private static double distanceRatio(double repDistanceInSeedUnits) {
        return Math.max(0.1, repDistanceInSeedUnits / 100.0);
    }

    private static double lerp(double a, double b, double t) {
        if (t <= 0) return a;
        if (t >= 1) return b;
        return a + (b - a) * t;
    }

    /** Round seconds to the nearest 5-second boundary (ties round up). */
    private static int roundToNearest5(int secs) {
        int rem = secs % 5;
        if (rem < 0) rem += 5;
        return (rem < 3) ? (secs - rem) : (secs + (5 - rem));
    }

    /**
     * Rest percentage as a function of effort and distance ratio r = rep/100.
     * Curves chosen from your MySwimPro sample + coaching intuition:
     *  - EASY: ~10% at <=100, ~18% by 400, taper toward ~5% for 1500+
     *  - ENDURANCE: ~4–5% nearly flat
     *  - THRESHOLD: ~6.7% at <=100 easing toward ~4% long
     *  - RACE/VO2/SPRINT: ~70% at <=100, ~25% by 400, ~5% very long
     */
    private static double restPercent(Effort e, double r) {
        if (e == null) return 0.06;

        switch (e) {
            case EASY: {
                if (r <= 1.0) return 0.10;
                if (r <= 4.0) return lerp(0.10, 0.18, (r - 1.0) / 3.0);
                return lerp(0.18, 0.05, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case ENDURANCE: {
                if (r <= 1.0) return 0.04;
                if (r <= 4.0) return lerp(0.04, 0.05, (r - 1.0) / 3.0);
                return lerp(0.05, 0.045, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case THRESHOLD: {
                if (r <= 1.0) return 0.067;
                if (r <= 4.0) return lerp(0.067, 0.055, (r - 1.0) / 3.0);
                return lerp(0.055, 0.040, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case RACE_PACE:
            case VO2_MAX:
            case SPRINT: {
                if (r <= 1.0) return 0.70;
                if (r <= 4.0) return lerp(0.70, 0.25, (r - 1.0) / 3.0);
                return lerp(0.25, 0.05, Math.min((r - 4.0) / 11.0, 1.0));
            }
            default:
                return 0.06;
        }
    }
}