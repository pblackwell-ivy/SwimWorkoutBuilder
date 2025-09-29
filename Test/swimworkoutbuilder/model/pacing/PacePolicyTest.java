package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.*;
import swimworkoutbuilder.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder.model.pacing.PacePolicy;

import static org.junit.jupiter.api.Assertions.*;
import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

class PacePolicyTest {

    @Test
    void interfaceContractBasicCall() {
        Swimmer sw = new Swimmer("P", "B", "", "");
        sw.updateSeedTime(StrokeType.FREESTYLE, new SeedPace100(80.0, CourseUnit.YARDS));

        Workout w = new Workout(sw.getId(), "Test", Course.SCY, "", 0);
        SwimSet s = new SwimSet(StrokeType.FREESTYLE, 2, yardsToMeters(50), Effort.THRESHOLD, "");

        PacePolicy p = new DefaultPacePolicy();

        // Should not throw, should return reasonable numbers
        double g1 = p.goalSeconds(w, s, sw, 1);
        int r1 = p.restSeconds(w, s, sw, 1);
        int i1 = p.intervalSeconds(w, s, sw, 1);

        assertTrue(g1 > 0);
        assertTrue(r1 > 0);
        assertEquals(Math.round(g1) + r1, i1);
    }
}