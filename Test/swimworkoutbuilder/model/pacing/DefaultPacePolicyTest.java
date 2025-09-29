package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.*;
import swimworkoutbuilder.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder.model.pacing.PacePolicy;

import static org.junit.jupiter.api.Assertions.*;
import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

class DefaultPacePolicyTest {

    @Test
    void goalIntervalRestForSCY_usesYardSeedAndMeterSet() {
        // Swimmer w/ FREESTYLE 78.0s per 100 yards
        Swimmer sw = new Swimmer("Test", "User", "", "");
        sw.updateSeedTime(StrokeType.FREESTYLE, new SeedPace100(78.0, CourseUnit.YARDS));

        // Workout SCY
        Workout w = new Workout(sw.getId(), "SCY Test", Course.SCY, "", 0);

        // Set: 1 x 50 yards (stored as meters internally)
        SwimSet set = new SwimSet(StrokeType.FREESTYLE, 1, yardsToMeters(50), Effort.RACE_PACE, "");

        PacePolicy policy = new DefaultPacePolicy();

        double goal = policy.goalSeconds(w, set, sw, 1);
        int rest = policy.restSeconds(w, set, sw, 1);
        int interval = policy.intervalSeconds(w, set, sw, 1);

        // expected: base 78 * (50/100) = 39.0; race pace multiplier 0.98 => 38.22s
        assertEquals(38.22, goal, 0.3);        // allow tolerance for rounding/float
        assertEquals(30, rest);                // RACE_PACE -> 30s
        assertEquals(Math.round(goal) + rest, interval);
    }

    @Test
    void effortMappings() {
        PacePolicy policy = new DefaultPacePolicy();
        Swimmer sw = new Swimmer("A", "B", "", "");
        sw.updateSeedTime(StrokeType.FREESTYLE, new SeedPace100(90.0, CourseUnit.METERS));
        Workout w = new Workout(sw.getId(), "X", Course.LCM, "", 0);
        SwimSet set = new SwimSet(StrokeType.FREESTYLE, 1, 100, Effort.SPRINT, "");

        assertEquals(40, policy.restSeconds(w, set, sw, 1)); // SPRINT mapping
    }
}