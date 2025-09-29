package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.*;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static swimworkoutbuilder.model.utils.Distance.yardsToMeters;

class WorkoutAggregationTest {

    @Test
    void distanceAggregationAndReorder() {
        Swimmer sw = new Swimmer("P", "B", "", "");
        Workout w = new Workout(sw.getId(), "Agg Test", Course.SCY, "", 0);

        SetGroup g1 = new SetGroup("Warmup", 1, 1);
        g1.addSet(new SwimSet(StrokeType.FREESTYLE, 4, yardsToMeters(50), Effort.EASY, ""));
        SetGroup g2 = new SetGroup("Main", 2, 2);
        g2.addSet(new SwimSet(StrokeType.FREESTYLE, 8, yardsToMeters(25), Effort.THRESHOLD, ""));

        Collections.addAll(w.getGroups(), g1, g2);

        int expectedSinglePass = g1.singlePassDistanceMeters() + g2.singlePassDistanceMeters();
        int expectedTotal = g1.totalDistanceMeters() + g2.totalDistanceMeters();
        assertEquals(expectedSinglePass, w.singlePassDistanceMeters());
        assertEquals(expectedTotal, w.totalDistanceMeters());

        // reorder
        w.swapGroups(0, 1);
        assertEquals("Main", w.getGroups().get(0).getName());
        assertEquals("Warmup", w.getGroups().get(1).getName());

        // move back
        w.moveGroup(0, 1);
        assertEquals("Warmup", w.getGroups().get(0).getName());
        assertEquals("Main", w.getGroups().get(1).getName());
    }
}