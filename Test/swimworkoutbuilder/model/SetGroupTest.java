package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.SetGroup;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;
import swimworkoutbuilder.model.utils.Distance;

import static org.junit.jupiter.api.Assertions.*;
import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

class SetGroupTest {

    @Test
    void addRemoveAndDistances() {
        SetGroup g = new SetGroup("Warmup", 2, 1);
        assertEquals(0, g.getSets().size());
        g.addSet(new SwimSet(StrokeType.FREESTYLE, 4, yardsToMeters(50), Effort.EASY, ""));
        g.addSet(new SwimSet(StrokeType.FREE_KICK, 2, yardsToMeters(25), Effort.EASY, ""));
        assertEquals(2, g.getSets().size());

        // single pass distance in meters
        int expectedSinglePass =
                4 * yardsToMeters(50) +
                        2 * yardsToMeters(25);
        assertEquals(expectedSinglePass, g.singlePassDistanceMeters());

        // group repeats = 2
        assertEquals(expectedSinglePass * 2, g.totalDistanceMeters());

        // remove
        SwimSet first = g.getSets().get(0);
        g.removeSet(first);
        assertEquals(1, g.getSets().size());
    }
}