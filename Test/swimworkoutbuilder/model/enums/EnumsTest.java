package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.enums.Course;
import swimworkoutbuilder.model.enums.CourseUnit;
import swimworkoutbuilder.model.enums.Effort;
import swimworkoutbuilder.model.enums.StrokeType;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {

    @Test
    void courseAndUnitsExist() {
        assertNotNull(Course.SCY);
        assertNotNull(Course.SCM);
        assertNotNull(Course.LCM);
        assertNotNull(CourseUnit.YARDS);
        assertNotNull(CourseUnit.METERS);
    }

    @Test
    void strokeTypesPresent() {
        assertNotNull(StrokeType.FREESTYLE);
        assertNotNull(StrokeType.BACKSTROKE);
        assertNotNull(StrokeType.BREASTSTROKE);
        assertNotNull(StrokeType.BUTTERFLY);
        assertNotNull(StrokeType.INDIVIDUAL_MEDLEY);
        assertNotNull(StrokeType.FREE_KICK);
        assertNotNull(StrokeType.DRILL);
    }

    @Test
    void effortLabelsPresent() {
        assertEquals("Easy", Effort.EASY.getLabel());
        assertEquals("Endurance", Effort.ENDURANCE.getLabel());
        assertEquals("Threshold", Effort.THRESHOLD.getLabel());
        assertEquals("Race Pace", Effort.RACE_PACE.getLabel());
        assertEquals("VO2 Max", Effort.VO2_MAX.getLabel());
        assertEquals("Sprint", Effort.SPRINT.getLabel());
    }
}