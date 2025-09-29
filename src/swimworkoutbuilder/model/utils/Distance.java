package swimworkoutbuilder.model.utils;

import swimworkoutbuilder.model.enums.CourseUnit;

public final class Distance {
    private Distance() {}
    public static double metersToSeedUnits(double meters, CourseUnit seedUnit) {
        return (seedUnit == CourseUnit.YARDS) ? meters / 0.9144 : meters;
    }
    public static double distance100s(double distanceInSeedUnits) {
        return distanceInSeedUnits / 100.0;
    }
    public static int yardsToMeters(int yards) {
        return (int) Math.round(yards * 0.9144);
    }
}