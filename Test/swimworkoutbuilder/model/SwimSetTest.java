package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.SwimSet;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;

import static org.junit.jupiter.api.Assertions.*;

class SwimSetTest {

    @Test
    void constructsAndExposesFields() {
        SwimSet s = new SwimSet(StrokeType.FREESTYLE, 4, 46, Effort.ENDURANCE, "desc");
        assertEquals(StrokeType.FREESTYLE, s.getStroke());
        assertEquals(4, s.getReps());
        assertEquals(46, s.getDistancePerRepMeters());
        assertEquals(Effort.ENDURANCE, s.getEffort());
        assertEquals("desc", s.getNotes());
    }

    @Test
    void guardsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> new SwimSet(StrokeType.FREESTYLE, 0, 46, Effort.EASY, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new SwimSet(StrokeType.FREESTYLE, 1, 0, Effort.EASY, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new SwimSet(StrokeType.FREESTYLE, 1, -5, Effort.EASY, ""));
    }
}