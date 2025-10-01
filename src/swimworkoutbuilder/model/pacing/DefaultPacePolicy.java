package swimworkoutbuilder.model.pacing;

import swimworkoutbuilder.model.SeedPace100;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.DistanceFactors;   // NEW
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.Equipment;         // NEW
import swimworkoutbuilder.model.enums.StrokeType;

import java.util.Objects;
import java.util.Set;

/**
 * Multiplier-based MVP policy for computing goal, interval, and rest.  The pace established
 * in the seed for the stroke represents the swimmer's actual or projected personal best time and
 * is used as a baseline. Effort, Distance, Course, and Equipment multipliers are applied to
 * calculate the target goal time, rest time, and interval time.
 *
 * Formulas:
 *   goal time =
 *     seed_per_100 (in seed units)
 *     × effort.paceMultiplier()
 *     × DistanceFactors.forDistance(distanceMeters)
 *     × workout.getCourse().multiplier()
 *     × product(equipment multipliers)
 *     × (repeatDistance expressed in seed units / 100.0)
 *
 *   rest    = effort.restAllowanceSec()
 *   interval = round(goal) + rest
 *
 * Notes:
 * - Distances are stored internally in meters in SwimSet.
 * - We convert the repeat distance into the *seed’s units* (yards/meters) before scaling by /100.
 * - Course multiplier captures performance differences (turns/underwater) separate from unit conversion.
 */
public class DefaultPacePolicy implements PacePolicy {

    private static final double YARD_TO_METER = 0.9144;
    private static final double METER_TO_YARD = 1.0 / YARD_TO_METER;

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
        double mEffort   = (set.getEffort() == null) ? 1.0 : set.getEffort().paceMultiplier();
        double mDist     = DistanceFactors.forDistance(set.getDistancePerRepMeters());
        double mCourse   = workout.getCourse().multiplier();
        double mEquip    = equipmentProduct(set.getEquipment());

        // 4) Scale seed by distance/100 in seed units, then apply multipliers
        double distanceScale = repDistanceInSeedUnits / 100.0;
        return seedPer100Sec * distanceScale * mEffort * mDist * mCourse * mEquip;
    }

    @Override
    public int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int goalRounded = (int)Math.round(goalSeconds(workout, set, swimmer, repIndex));
        int rest = restSeconds(workout, set, swimmer, repIndex);
        return goalRounded + rest;
    }

    @Override
    public int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        Effort e = set.getEffort();
        return (e == null) ? 20 : e.restAllowanceSec();
    }

    @Override
    public String timingLabel(Workout workout, SwimSet set) {
        Effort e = set.getEffort();
        int rest = (e == null) ? 20 : e.restAllowanceSec();
        return "rest :" + rest;
    }

    // --- helpers ---

    private static double equipmentProduct(Set<Equipment> equipment) {
        if (equipment == null || equipment.isEmpty()) return 1.0;
        double m = 1.0;
        for (Equipment eq : equipment) {
            if (eq != null) m *= eq.multiplier();
        }
        return m;
    }
}