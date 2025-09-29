package swimworkoutbuilder.model.pacing;

import swimworkoutbuilder.model.SeedPace100;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.Workout;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;

import java.util.Objects;

/**
 * Default MVP policy for computing goal, interval, and rest.
 *
 * Rules:
 *  - goal = seed pace per 100 (converted if needed) * distance/100 * effort multiplier
 *  - rest = fixed seconds mapped by Effort
 *  - interval = round(goal) + rest
 *
 * Distances are stored internally in meters. Seeds may be in yards or meters.
 */
public class DefaultPacePolicy implements PacePolicy {

    private static final double YARD_TO_METER = 0.9144;
    private static final double METER_TO_YARD = 1.0 / YARD_TO_METER;

    @Override
    public double goalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        Objects.requireNonNull(workout, "workout");
        Objects.requireNonNull(set, "set");
        Objects.requireNonNull(swimmer, "swimmer");

        StrokeType stroke = set.getStroke();
        SeedPace100 seed = swimmer.getSeedTime(stroke);
        if (seed == null) {
            throw new IllegalStateException("Missing seed for stroke: " + stroke);
        }

        double seedPer100 = seed.getSeedTimeSec();   // seconds per 100 in the seed’s unit
        CourseUnit seedUnit = seed.getCourseUnit();

        // Convert rep distance into the seed’s unit
        double repDistanceInSeedUnit =
                (seedUnit == CourseUnit.YARDS)
                        ? set.getDistancePerRepMeters() * METER_TO_YARD
                        : set.getDistancePerRepMeters();

        double base = seedPer100 * (repDistanceInSeedUnit / 100.0);
        return base * effortMultiplier(set.getEffort());
    }

    @Override
    public int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int goalRounded = (int) Math.round(goalSeconds(workout, set, swimmer, repIndex));
        int rest = restSeconds(workout, set, swimmer, repIndex);
        return goalRounded + rest;
    }

    @Override
    public int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        return effortRestSeconds(set.getEffort());
    }

    @Override
    public String timingLabel(Workout workout, SwimSet set) {
        int rest = effortRestSeconds(set.getEffort());
        return "rest :" + rest;
    }

    // --- Effort mappings aligned to your enum ---

    private int effortRestSeconds(Effort e) {
        if (e == null) return 20;
        switch (e) {
            case EASY:      return 15;
            case ENDURANCE: return 20;
            case THRESHOLD: return 25;
            case RACE_PACE: return 30;
            case VO2_MAX:   return 35;
            case SPRINT:    return 40;
            default:        return 20;
        }
    }

    private double effortMultiplier(Effort e) {
        if (e == null) return 1.0;
        switch (e) {
            case EASY:      return 1.05;
            case ENDURANCE: return 1.02;
            case THRESHOLD: return 1.00;
            case RACE_PACE: return 0.98;
            case VO2_MAX:   return 0.97;
            case SPRINT:    return 0.95;
            default:        return 1.0;
        }
    }
}