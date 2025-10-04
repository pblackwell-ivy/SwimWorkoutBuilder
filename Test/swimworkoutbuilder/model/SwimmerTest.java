package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.Swimmer;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.StrokeType;

import static org.junit.jupiter.api.Assertions.*;

class SwimmerTest {

    @Test
    void createAndUpdateSeeds() {
        Swimmer sw = new Swimmer("Parker", "Blackwell", "", "Indy Aquatic Masters");
        assertNotNull(sw.getId());
        assertFalse(sw.hasSeed(StrokeType.FREESTYLE));

        sw.updateSeedTime(StrokeType.FREESTYLE, new SeedPace100(78.0, CourseUnit.YARDS));
        assertTrue(sw.hasSeed(StrokeType.FREESTYLE));
        assertEquals(78.0, sw.getSeedTime(StrokeType.FREESTYLE).getSeedTimeSec(), 1e-9);

        sw.clearSeed(StrokeType.FREESTYLE);
        assertFalse(sw.hasSeed(StrokeType.FREESTYLE));
        assertNull(sw.getSeedTime(StrokeType.FREESTYLE));
    }
}