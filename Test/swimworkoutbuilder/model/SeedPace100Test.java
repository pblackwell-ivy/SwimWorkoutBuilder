package swimworkoutbuilder.tests;

import org.junit.jupiter.api.Test;
import swimworkoutbuilder.model.enums.CourseUnit;

class SeedPace100Test {

    @Test
    void seedStoresSecondsAndUnit() {
        SeedPace100 s = new SeedPace100(78.0, CourseUnit.YARDS);
        assertEquals(78.0, s.getSeedTimeSec(), 1e-9);
        assertEquals(CourseUnit.YARDS, s.getCourseUnit());
    }

    @Test
    void settersUpdateAndTouchTimestamp() throws InterruptedException {
        SeedPace100 s = new SeedPace100(80.0, CourseUnit.METERS);
        var t1 = s.getLastUpdated();
        Thread.sleep(5);
        s.setSeedTimeSec(79.5);
        var t2 = s.getLastUpdated();
        assertTrue(t2.after(t1));

        Thread.sleep(5);
        s.setCourseUnit(CourseUnit.YARDS);
        var t3 = s.getLastUpdated();
        assertTrue(t3.after(t2));
        assertEquals(CourseUnit.YARDS, s.getCourseUnit());
    }
}