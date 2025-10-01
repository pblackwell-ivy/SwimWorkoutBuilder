package swimworkoutbuilder.model.enums;

/**
 * DistanceFactors provides multipliers to adjust target pace
 * depending on repeat distance. Short reps are faster than seed,
 * longer reps trend slower.
 *
 * To do:  add a fallback calculation for distances outside the buckets.
 */
public enum DistanceFactors {
    D25(25,     0.92),
    D50(50,     0.94),
    D75(75,     0.97),
    D100(100,   1.00),
    D200(200,   1.05),
    D400(400,   1.10),
    D800(800,   1.15),
    D1500(1500, 1.20);

    private final int meters;
    private final double multiplier;

    DistanceFactors(int meters, double multiplier) {
        this.meters = meters;
        this.multiplier = multiplier;
    }

    public int meters() { return meters; }
    public double multiplier() { return multiplier; }

    /**
     * Get the pace multiplier for an arbitrary distance in meters.
     * Uses buckets to approximate performance trends.
     */
    public static double forDistance(int meters) {
        if (meters <= 25)   return D25.multiplier;
        if (meters <= 50)   return D50.multiplier;
        if (meters <= 75)   return D75.multiplier;
        if (meters <= 100)  return D100.multiplier;
        if (meters <= 200)  return D200.multiplier;
        if (meters <= 400)  return D400.multiplier;
        if (meters <= 800)  return D800.multiplier;
        return D1500.multiplier; // default for anything longer
    }

    @Override
    public String toString() {
        return meters + "m (" + multiplier + ")";
    }
}